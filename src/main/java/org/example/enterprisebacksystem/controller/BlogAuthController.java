package org.example.enterprisebacksystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.common.annotation.AuditLog;
import org.example.enterprisebacksystem.common.exception.BizException;
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.domain.Role;
import org.example.enterprisebacksystem.domain.User;
import org.example.enterprisebacksystem.domain.UserRole;
import org.example.enterprisebacksystem.dto.auth.LoginReq;
import org.example.enterprisebacksystem.dto.auth.LoginResp;
import org.example.enterprisebacksystem.dto.blog.BlogProfileResp;
import org.example.enterprisebacksystem.dto.blog.BlogProfileUpdateReq;
import org.example.enterprisebacksystem.dto.blog.BlogRegisterReq;
import org.example.enterprisebacksystem.mapper.UserRoleMapper;
import org.example.enterprisebacksystem.security.LoginUser;
import org.example.enterprisebacksystem.service.AuthService;
import org.example.enterprisebacksystem.service.BlogCaptchaService;
import org.example.enterprisebacksystem.mapper.PermissionMapper;
import org.example.enterprisebacksystem.service.RoleService;
import org.example.enterprisebacksystem.service.TurnstileService;
import org.example.enterprisebacksystem.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/blog/auth")
@RequiredArgsConstructor
public class BlogAuthController {
    private final AuthService authService;
    private final UserService userService;
    private final RoleService roleService;
    private final UserRoleMapper userRoleMapper;
    private final PermissionMapper permissionMapper;
    private final BlogCaptchaService blogCaptchaService;
    private final TurnstileService turnstileService;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ApiResponse<LoginResp> login(@Valid @RequestBody LoginReq req) {
        return ApiResponse.ok(authService.login(req));
    }

    @PostMapping("/register")
    @Transactional
    @AuditLog("博客普通用户注册")
    public ApiResponse<LoginResp> register(@Valid @RequestBody BlogRegisterReq req, HttpServletRequest request) {
        if (!blogCaptchaService.verify(req.getCaptchaId(), req.getCaptchaAnswer())) {
            throw new BizException(40000, "验证码错误或已过期");
        }
        if (!turnstileService.verify(req.getTurnstileToken(), clientIp(request))) {
            throw new BizException(40000, "Cloudflare人机验证失败");
        }

        long exists = userService.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername()));
        if (exists > 0) {
            throw new BizException(40000, "用户名已存在");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname() == null || req.getNickname().isBlank() ? req.getUsername() : req.getNickname());
        user.setEmail(req.getEmail());
        user.setStatus(1);
        user.setDeleted(0);
        userService.save(user);

        Role normalRole = roleService.getOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getCode, "user")
                .last("LIMIT 1"));
        if (normalRole == null) {
            throw new BizException(40000, "普通用户角色未初始化");
        }

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(normalRole.getId());
        userRoleMapper.insert(userRole);

        LoginReq loginReq = new LoginReq();
        loginReq.setUsername(req.getUsername());
        loginReq.setPassword(req.getPassword());
        return ApiResponse.ok(authService.login(loginReq));
    }

    @GetMapping("/site-profile")
    public ApiResponse<BlogProfileResp> siteProfile() {
        User owner = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getSiteOwner, 1)
                .eq(User::getStatus, 1)
                .last("LIMIT 1"));
        if (owner == null) {
            owner = userService.getOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, "zhaoyoung")
                    .last("LIMIT 1"));
        }
        if (owner == null) {
            throw new BizException(40400, "站主账号未初始化");
        }
        return ApiResponse.ok(toProfileResp(owner));
    }

    @GetMapping("/me")
    public ApiResponse<BlogProfileResp> me() {
        return ApiResponse.ok(toProfileResp(currentUser()));
    }

    @PutMapping("/me")
    @AuditLog("修改博客用户资料")
    public ApiResponse<BlogProfileResp> updateMe(@Valid @RequestBody BlogProfileUpdateReq req) {
        User current = currentUser();
        User update = new User();
        update.setId(current.getId());
        update.setNickname(blankToNull(req.getNickname()));
        update.setEmail(blankToNull(req.getEmail()));
        update.setAvatar(normalizeAvatar(req.getAvatar()));
        update.setBio(blankToNull(req.getBio()));
        userService.updateById(update);
        return ApiResponse.ok(toProfileResp(userService.getById(current.getId())));
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("CF-Connecting-IP");
        if (forwarded == null || forwarded.isBlank()) {
            forwarded = request.getHeader("X-Forwarded-For");
        }
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            User user = userService.getById(loginUser.getUserId());
            if (user != null) {
                return user;
            }
        }
        throw new BizException(40100, "请先登录");
    }

    private BlogProfileResp toProfileResp(User user) {
        List<String> roles = permissionMapper.selectRoleCodesByUserId(user.getId());
        List<String> permissions = permissionMapper.selectPermissionCodesByUserId(user.getId());
        return new BlogProfileResp(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getAvatar(),
                user.getBio(),
                user.getSiteOwner(),
                user.getCreateTime(),
                roles == null ? List.of() : roles,
                permissions == null ? List.of() : permissions
        );
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeAvatar(String avatar) {
        String value = blankToNull(avatar);
        if (value == null) {
            return null;
        }
        if (!value.startsWith("data:image/") && !value.startsWith("http://") && !value.startsWith("https://") && !value.startsWith("/")) {
            throw new BizException(40000, "头像必须是图片 data URL、站内路径或 http(s) 地址");
        }
        return value;
    }
}
