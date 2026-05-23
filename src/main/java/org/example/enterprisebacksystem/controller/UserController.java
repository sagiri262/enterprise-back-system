package org.example.enterprisebacksystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.common.annotation.AuditLog;
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.common.api.ErrorCode;
import org.example.enterprisebacksystem.common.enums.UserStatus;
import org.example.enterprisebacksystem.common.exception.BizException;
import org.example.enterprisebacksystem.domain.User;
import org.example.enterprisebacksystem.dto.user.UserPageReq;
import org.example.enterprisebacksystem.dto.user.UserSaveReq;
import org.example.enterprisebacksystem.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasAuthority('user:list')")
    public ApiResponse<Page<User>> page(@Valid UserPageReq req) {
        Page<User> pageParam = new Page<>(req.getPage(), req.getSize());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(req.getKeyword() != null && !req.getKeyword().isBlank(), w -> w
                        .like(User::getUsername, req.getKeyword()).or()
                        .like(User::getNickname, req.getKeyword()).or()
                        .like(User::getEmail, req.getKeyword()))
                .eq(req.getStatus() != null, User::getStatus, req.getStatus())
                .orderByDesc(User::getCreateTime);

        return ApiResponse.ok(userService.page(pageParam, wrapper));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    @AuditLog("新增用户")
    public ApiResponse<Boolean> create(@Valid @RequestBody UserSaveReq req) {
        validateStatus(req.getStatus());

        User user = new User();
        BeanUtils.copyProperties(req, user);
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        user.setDeleted(0);

        return ApiResponse.ok(userService.save(user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    @AuditLog("修改用户")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody UserSaveReq req) {
        validateStatus(req.getStatus());

        User user = new User();
        BeanUtils.copyProperties(req, user);
        user.setId(id);
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        } else {
            user.setPassword(null);
        }

        return ApiResponse.ok(userService.updateById(user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    @AuditLog("删除用户")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        return ApiResponse.ok(userService.removeById(id));
    }

    private void validateStatus(Integer status) {
        if (!UserStatus.isValid(status)) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "用户状态只能是 0 或 1");
        }
    }
}
