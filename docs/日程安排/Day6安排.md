可以。按仓库文档排期，项目目标是 Day4 做 Spring Security 鉴权、Day5 做 AOP 审计日志、Day6 做分页/批量授权/枚举规范/测试。当前代码里 Day4/Day5 已经有雏形，但还没完全跑通：例如 `AuthController` 当前路径是 `/common/api/auth`，而安全白名单放行的是 `/api/auth/**`；`LoginUser` 的权限泛型和 `Collectors` 导入也会导致编译问题；`LogAspect` 目前还没有 `saveLog` 实现。

下面这些代码建议按顺序粘贴。先把 Day4/Day5 修到可编译、可登录、可鉴权、可审计，再补 Day6 的批量授权和枚举规范。

## 1. pom.xml 补充 AOP 依赖

你现在只有 `aspectjrt`，Spring AOP 切面建议加这个：

```xml
<!-- AOP 审计日志 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

---

## 2. 替换 `LoginUser.java`

路径：

```text
src/main/java/org/example/enterprisebacksystem/security/LoginUser.java
```

```java
package org.example.enterprisebacksystem.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enterprisebacksystem.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements UserDetails {

    private User user;

    /**
     * 这里保存权限字符串：
     * 例如 user:list、user:create、ROLE_admin
     */
    private List<String> permissions;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public Long getUserId() {
        return user == null ? null : user.getId();
    }

    @Override
    public String getPassword() {
        return user == null ? null : user.getPassword();
    }

    @Override
    public String getUsername() {
        return user == null ? null : user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user != null && user.getStatus() != null && user.getStatus() == 1;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user != null && user.getStatus() != null && user.getStatus() == 1;
    }
}
```

---

## 3. 替换 `PermissionMapper.java`

路径：

```text
src/main/java/org/example/enterprisebacksystem/mapper/PermissionMapper.java
```

```java
package org.example.enterprisebacksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.example.enterprisebacksystem.domain.Permission;

import java.util.List;

public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据用户 ID 查询权限编码列表，例如：
     * user:list、user:create、role:assign-permission
     */
    @Select("""
            SELECT DISTINCT p.code
            FROM sys_permission p
            INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
            INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND p.code IS NOT NULL
              AND p.code <> ''
            """)
    List<String> selectPermissionCodesByUserId(Long userId);

    /**
     * 根据用户 ID 查询角色编码列表，例如：
     * admin、manager、user
     */
    @Select("""
            SELECT DISTINCT r.code
            FROM sys_role r
            INNER JOIN sys_user_role ur ON r.id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND r.code IS NOT NULL
              AND r.code <> ''
            """)
    List<String> selectRoleCodesByUserId(Long userId);
}
```

---

## 4. 替换 `UserDetailsServiceImpl.java`

路径：

```text
src/main/java/org/example/enterprisebacksystem/security/UserDetailsServiceImpl.java
```

```java
package org.example.enterprisebacksystem.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.domain.User;
import org.example.enterprisebacksystem.mapper.PermissionMapper;
import org.example.enterprisebacksystem.mapper.UserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final PermissionMapper permissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .last("LIMIT 1")
        );

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        List<String> permissions = permissionMapper.selectPermissionCodesByUserId(user.getId());
        List<String> roles = permissionMapper.selectRoleCodesByUserId(user.getId());

        List<String> allAuthorities = new ArrayList<>();
        if (permissions != null) {
            allAuthorities.addAll(permissions);
        }

        if (roles != null) {
            roles.forEach(role -> {
                if (role != null && !role.isBlank()) {
                    allAuthorities.add("ROLE_" + role);
                }
            });
        }

        return new LoginUser(user, allAuthorities);
    }
}
```

---

## 5. 替换 `JwtAuthenticationTokenFilter.java`

路径：

```text
src/main/java/org/example/enterprisebacksystem/security/JwtAuthenticationTokenFilter.java
```

```java
package org.example.enterprisebacksystem.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.common.utils.JwtUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private static final String TOKEN_PREFIX = "Bearer ";

    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = request.getHeader("Authorization");

        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (token.startsWith(TOKEN_PREFIX)) {
            token = token.substring(TOKEN_PREFIX.length());
        }

        try {
            Claims claims = JwtUtils.parseToken(token);
            String username = claims.getSubject();

            if (StringUtils.hasText(username)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
```

---

## 6. 替换 `SecurityConfig.java`

路径：

```text
src/main/java/org/example/enterprisebacksystem/config/SecurityConfig.java
```

```java
package org.example.enterprisebacksystem.config;

import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.security.JwtAuthenticationTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

---

## 7. 新增 `UserService.java`

你当前仓库里缺 `UserService`，但登录测试注册需要它。

路径：

```text
src/main/java/org/example/enterprisebacksystem/service/UserService.java
```

```java
package org.example.enterprisebacksystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.enterprisebacksystem.domain.User;

public interface UserService extends IService<User> {
}
```

---

## 8. 替换 `UserServiceImpl.java`

路径：

```text
src/main/java/org/example/enterprisebacksystem/service/impl/UserServiceImpl.java
```

```java
package org.example.enterprisebacksystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.enterprisebacksystem.domain.User;
import org.example.enterprisebacksystem.mapper.UserMapper;
import org.example.enterprisebacksystem.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
```

---

## 9. 替换 `AuthController.java`

把登录路径改回 `/api/auth`，否则不会命中 Security 白名单。

路径：

```text
src/main/java/org/example/enterprisebacksystem/controller/AuthController.java
```

```java
package org.example.enterprisebacksystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.common.annotation.AuditLog;
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.domain.User;
import org.example.enterprisebacksystem.dto.auth.LoginReq;
import org.example.enterprisebacksystem.dto.auth.LoginResp;
import org.example.enterprisebacksystem.service.AuthService;
import org.example.enterprisebacksystem.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @AuditLog("用户登录")
    public ApiResponse<LoginResp> login(@Valid @RequestBody LoginReq req) {
        return ApiResponse.ok(authService.login(req));
    }

    /**
     * 开发测试用：生成 BCrypt 密码用户。
     * 后续正式项目可以删除或只在 dev 环境开放。
     */
    @PostMapping("/register-test")
    public ApiResponse<String> registerTest(@Valid @RequestBody LoginReq req) {
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname("测试用户");
        user.setStatus(1);
        user.setDeleted(0);

        userService.save(user);
        return ApiResponse.ok("注册成功");
    }
}
```

---

## 10. 替换 `AuditLog.java`

路径：

```text
src/main/java/org/example/enterprisebacksystem/domain/AuditLog.java
```

```java
package org.example.enterprisebacksystem.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_audit_log")
public class AuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String username;

    /**
     * 操作描述
     */
    private String action;

    /**
     * 请求方法：GET、POST、PUT、DELETE
     */
    private String method;

    /**
     * 请求路径
     */
    private String uri;

    /**
     * 请求参数，已脱敏
     */
    private String params;

    private String ipAddress;

    /**
     * 1 成功，0 失败
     */
    private Integer status;

    /**
     * 耗时，单位 ms
     */
    private Long costTime;

    private LocalDateTime createTime;
}
```

数据库如果没有这些字段，补一下：

```sql
ALTER TABLE sys_audit_log
    ADD COLUMN uri varchar(255) NULL COMMENT '请求路径' AFTER method,
    ADD COLUMN params text NULL COMMENT '请求参数' AFTER uri,
    ADD COLUMN status tinyint DEFAULT 1 COMMENT '状态 1成功 0失败' AFTER ip_address,
    ADD COLUMN cost_time bigint DEFAULT 0 COMMENT '耗时ms' AFTER status;
```

---

## 11. 替换 `LogAspect.java`

路径：

```text
src/main/java/org/example/enterprisebacksystem/common/aspect/LogAspect.java
```

```java
package org.example.enterprisebacksystem.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.enterprisebacksystem.common.annotation.AuditLog;
import org.example.enterprisebacksystem.mapper.AuditLogMapper;
import org.example.enterprisebacksystem.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LogAspect {

    private final AuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint point, AuditLog auditLog) throws Throwable {
        long beginTime = System.currentTimeMillis();
        int status = 1;

        try {
            return point.proceed();
        } catch (Throwable e) {
            status = 0;
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - beginTime;
            saveLog(point, auditLog, costTime, status);
        }
    }

    private void saveLog(ProceedingJoinPoint point, AuditLog auditLog, long costTime, int status) {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            org.example.enterprisebacksystem.domain.AuditLog logEntity =
                    new org.example.enterprisebacksystem.domain.AuditLog();

            logEntity.setAction(auditLog.value());
            logEntity.setMethod(request.getMethod());
            logEntity.setUri(request.getRequestURI());
            logEntity.setIpAddress(getClientIp(request));
            logEntity.setCostTime(costTime);
            logEntity.setStatus(status);
            logEntity.setCreateTime(LocalDateTime.now());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
                logEntity.setUserId(loginUser.getUserId());
                logEntity.setUsername(loginUser.getUsername());
            }

            String params = objectMapper.writeValueAsString(point.getArgs());
            logEntity.setParams(maskSensitiveParams(params));

            auditLogMapper.insert(logEntity);
        } catch (Exception e) {
            log.error("审计日志记录失败", e);
        }
    }

    private String maskSensitiveParams(String params) {
        if (params == null) {
            return null;
        }

        String lower = params.toLowerCase();
        if (lower.contains("password") || lower.contains("token")) {
            return "{\"message\":\"参数包含敏感信息，已隐藏\"}";
        }

        if (params.length() > 2000) {
            return params.substring(0, 2000);
        }

        return params;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0];
        }

        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }
}
```

---

# Day6 代码：批量授权 + 枚举规范

## 12. 新增 `UserStatus.java`

路径：

```text
src/main/java/org/example/enterprisebacksystem/common/enums/UserStatus.java
```

```java
package org.example.enterprisebacksystem.common.enums;

import lombok.Getter;

@Getter
public enum UserStatus {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final int code;
    private final String desc;

    UserStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
```

---

## 13. 新增 `PermissionType.java`

路径：

```text
src/main/java/org/example/enterprisebacksystem/common/enums/PermissionType.java
```

```java
package org.example.enterprisebacksystem.common.enums;

import lombok.Getter;

@Getter
public enum PermissionType {

    MENU(1, "菜单"),
    BUTTON(2, "按钮");

    private final int code;
    private final String desc;

    PermissionType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
```

---

## 14. 新增 `UserRole.java`

路径：

```text
src/main/java/org/example/enterprisebacksystem/domain/UserRole.java
```

```java
package org.example.enterprisebacksystem.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user_role")
public class UserRole {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long roleId;
}
```

---

## 15. 新增 `RolePermission.java`

路径：

```text
src/main/java/org/example/enterprisebacksystem/domain/RolePermission.java
```

```java
package org.example.enterprisebacksystem.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_role_permission")
public class RolePermission {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roleId;
    private Long permissionId;
}
```

---

## 16. 新增 `UserRoleMapper.java`

路径：

```text
src/main/java/org/example/enterprisebacksystem/mapper/UserRoleMapper.java
```

```java
package org.example.enterprisebacksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.enterprisebacksystem.domain.UserRole;

public interface UserRoleMapper extends BaseMapper<UserRole> {
}
```

---

## 17. 新增 `RolePermissionMapper.java`

路径：

```text
src/main/java/org/example/enterprisebacksystem/mapper/RolePermissionMapper.java
```

```java
package org.example.enterprisebacksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.enterprisebacksystem.domain.RolePermission;

public interface RolePermissionMapper extends BaseMapper<RolePermission> {
}
```

---

## 18. 新增 `AssignUserRolesReq.java`

路径：

```text
src/main/java/org/example/enterprisebacksystem/dto/authz/AssignUserRolesReq.java
```

```java
package org.example.enterprisebacksystem.dto.authz;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AssignUserRolesReq {

    @NotEmpty(message = "角色 ID 列表不能为空")
    private List<Long> roleIds;
}
```

---

## 19. 新增 `AssignRolePermissionsReq.java`

路径：

```text
src/main/java/org/example/enterprisebacksystem/dto/authz/AssignRolePermissionsReq.java
```

```java
package org.example.enterprisebacksystem.dto.authz;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AssignRolePermissionsReq {

    @NotEmpty(message = "权限 ID 列表不能为空")
    private List<Long> permissionIds;
}
```

---

## 20. 新增 `AuthorizationService.java`

路径：

```text
src/main/java/org/example/enterprisebacksystem/service/AuthorizationService.java
```

```java
package org.example.enterprisebacksystem.service;

import java.util.List;

public interface AuthorizationService {

    void assignRolesToUser(Long userId, List<Long> roleIds);

    void assignPermissionsToRole(Long roleId, List<Long> permissionIds);
}
```

---

## 21. 新增 `AuthorizationServiceImpl.java`

路径：

```text
src/main/java/org/example/enterprisebacksystem/service/impl/AuthorizationServiceImpl.java
```

```java
package org.example.enterprisebacksystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.domain.RolePermission;
import org.example.enterprisebacksystem.domain.UserRole;
import org.example.enterprisebacksystem.mapper.RolePermissionMapper;
import org.example.enterprisebacksystem.mapper.UserRoleMapper;
import org.example.enterprisebacksystem.service.AuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, userId)
        );

        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }

        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, roleId)
        );

        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }

        for (Long permissionId : permissionIds) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermissionMapper.insert(rolePermission);
        }
    }
}
```

---

## 22. 新增 `AuthorizationController.java`

路径：

```text
src/main/java/org/example/enterprisebacksystem/controller/AuthorizationController.java
```

```java
package org.example.enterprisebacksystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.common.annotation.AuditLog;
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.dto.authz.AssignRolePermissionsReq;
import org.example.enterprisebacksystem.dto.authz.AssignUserRolesReq;
import org.example.enterprisebacksystem.service.AuthorizationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authz")
@RequiredArgsConstructor
public class AuthorizationController {

    private final AuthorizationService authorizationService;

    @PostMapping("/users/{userId}/roles")
    @PreAuthorize("hasAuthority('user:assign-role')")
    @AuditLog("给用户批量分配角色")
    public ApiResponse<Void> assignRolesToUser(
            @PathVariable Long userId,
            @Valid @RequestBody AssignUserRolesReq req
    ) {
        authorizationService.assignRolesToUser(userId, req.getRoleIds());
        return ApiResponse.ok();
    }

    @PostMapping("/roles/{roleId}/permissions")
    @PreAuthorize("hasAuthority('role:assign-permission')")
    @AuditLog("给角色批量分配权限")
    public ApiResponse<Void> assignPermissionsToRole(
            @PathVariable Long roleId,
            @Valid @RequestBody AssignRolePermissionsReq req
    ) {
        authorizationService.assignPermissionsToRole(roleId, req.getPermissionIds());
        return ApiResponse.ok();
    }
}
```

---

## 23. 建议补充初始化权限数据

否则 `@PreAuthorize` 会一直 403。

```sql
INSERT INTO sys_role (id, name, code, description, create_time, update_time)
VALUES
    (1, '管理员', 'admin', '系统管理员', NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO sys_permission (id, parent_id, name, code, type, path, create_time, update_time)
VALUES
    (1, 0, '用户列表', 'user:list', 2, NULL, NOW(), NOW()),
    (2, 0, '给用户分配角色', 'user:assign-role', 2, NULL, NOW(), NOW()),
    (3, 0, '给角色分配权限', 'role:assign-permission', 2, NULL, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), code = VALUES(code);

-- 假设你的测试用户 id = 1
INSERT INTO sys_user_role (user_id, role_id)
VALUES (1, 1);

INSERT INTO sys_role_permission (role_id, permission_id)
VALUES
    (1, 1),
    (1, 2),
    (1, 3);
```

---

## 24. 验收顺序

先调用：

```http
POST /api/auth/register-test
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

再登录：

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

拿到 token 后，之后请求头这样带：

```http
Authorization: Bearer 你的token
```

然后验证：

```http
GET /api/demo/data
```

再验证 Day6 批量授权：

```http
POST /api/authz/users/1/roles
Authorization: Bearer 你的token
Content-Type: application/json

{
  "roleIds": [1]
}
```

```http
POST /api/authz/roles/1/permissions
Authorization: Bearer 你的token
Content-Type: application/json

{
  "permissionIds": [1, 2, 3]
}
```

这样补完后，项目实际进度就从“Day4/Day5 半成品”推进到：登录认证、接口鉴权、不同角色返回不同结果、审计日志、批量授权这些核心闭环都能跑起来。


`authz` 不是必须的，是我为了区分 **认证 authentication** 和 **授权 authorization** 单独拆出来的目录。

你现在已有：

```text
dto/auth/LoginReq.java
dto/auth/LoginResp.java
```

这个 `auth` 更适合放“登录/注册/令牌”相关 DTO，比如：

```text
LoginReq
LoginResp
RegisterReq
RefreshTokenReq
```

而我新增的：

```text
dto/authz/AssignUserRolesReq.java
dto/authz/AssignRolePermissionsReq.java
```

这里的 `authz` 是 **authorization** 的常见缩写，表示“授权”。它放的是“给用户分配角色、给角色分配权限”这种权限管理请求，不是登录请求。

所以目录含义是：

```text
dto/auth      # 认证：你是谁，怎么登录
dto/authz     # 授权：你有什么权限，给谁分配什么角色/权限
```

不过对于你这个练习项目，完全可以不建 `authz`，直接放到已有的 `auth` 目录下，结构更简单。

例如改成：

```text
src/main/java/org/example/enterprisebacksystem/dto/auth/AssignUserRolesReq.java
src/main/java/org/example/enterprisebacksystem/dto/auth/AssignRolePermissionsReq.java
```

对应 package 改成：

```java
package org.example.enterprisebacksystem.dto.auth;
```

然后 `AuthorizationController` 里的 import 改成：

```java
import org.example.enterprisebacksystem.dto.auth.AssignRolePermissionsReq;
import org.example.enterprisebacksystem.dto.auth.AssignUserRolesReq;
```

结论：
`authz` 是为了语义更清楚，不是硬性要求。你想保持目录简单的话，直接放进现有 `dto/auth` 完全可以。
