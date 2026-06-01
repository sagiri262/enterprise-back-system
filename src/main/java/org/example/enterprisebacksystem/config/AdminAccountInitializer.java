package org.example.enterprisebacksystem.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enterprisebacksystem.domain.Permission;
import org.example.enterprisebacksystem.domain.Role;
import org.example.enterprisebacksystem.domain.RolePermission;
import org.example.enterprisebacksystem.domain.User;
import org.example.enterprisebacksystem.domain.UserRole;
import org.example.enterprisebacksystem.mapper.RolePermissionMapper;
import org.example.enterprisebacksystem.mapper.UserRoleMapper;
import org.example.enterprisebacksystem.service.PermissionService;
import org.example.enterprisebacksystem.service.RoleService;
import org.example.enterprisebacksystem.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements CommandLineRunner {
    private static final List<String> ADMIN_PERMISSION_CODES = List.of(
            "user:list",
            "user:create",
            "user:assign-role",
            "user:update",
            "user:delete",
            "role:list",
            "role:create",
            "role:update",
            "role:delete",
            "role:assign-permission",
            "permission:list",
            "permission:create",
            "permission:update",
            "permission:delete",
            "blog:article:list",
            "blog:article:create",
            "blog:article:update",
            "blog:article:delete",
            "blog:comment:create",
            "blog:comment:delete"
    );

    private final UserService userService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.admin.username:}")
    private String adminUsername;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.nickname:系统管理员}")
    private String adminNickname;

    @Value("${app.admin.update-password:false}")
    private boolean updatePassword;

    @Override
    public void run(String... args) {
        if (adminUsername == null || adminUsername.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.info("Admin account initializer skipped: app.admin.username or app.admin.password is not configured.");
            return;
        }
        if (adminPassword.length() < 16) {
            throw new IllegalStateException("app.admin.password must be at least 16 characters.");
        }

        Role adminRole = ensureAdminRole();
        ensureAdminPermissions(adminRole);
        User admin = ensureAdminUser();
        ensureUserRole(admin, adminRole);
    }

    private Role ensureAdminRole() {
        Role role = roleService.getOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getCode, "admin")
                .last("LIMIT 1"));
        if (role != null) {
            return role;
        }
        role = new Role();
        role.setName("超级管理员");
        role.setCode("admin");
        role.setDescription("拥有系统全部权限");
        roleService.save(role);
        return role;
    }

    private void ensureAdminPermissions(Role adminRole) {
        for (String code : ADMIN_PERMISSION_CODES) {
            Permission permission = permissionService.getOne(new LambdaQueryWrapper<Permission>()
                    .eq(Permission::getCode, code)
                    .last("LIMIT 1"));
            if (permission == null) {
                continue;
            }

            Long count = rolePermissionMapper.selectCount(new LambdaQueryWrapper<RolePermission>()
                    .eq(RolePermission::getRoleId, adminRole.getId())
                    .eq(RolePermission::getPermissionId, permission.getId()));
            if (count != null && count > 0) {
                continue;
            }

            RolePermission relation = new RolePermission();
            relation.setRoleId(adminRole.getId());
            relation.setPermissionId(permission.getId());
            rolePermissionMapper.insert(relation);
        }
    }

    private User ensureAdminUser() {
        String username = adminUsername.trim();
        User admin = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .last("LIMIT 1"));

        if (admin == null) {
            admin = new User();
            admin.setUsername(username);
            admin.setDeleted(0);
            admin.setPassword(passwordEncoder.encode(adminPassword));
        } else if (updatePassword || admin.getPassword() == null || admin.getPassword().isBlank()) {
            admin.setPassword(passwordEncoder.encode(adminPassword));
        } else if (!passwordEncoder.matches(adminPassword, admin.getPassword())) {
            log.info("Admin password is already set and app.admin.update-password=false, keeping existing password.");
        }

        admin.setNickname(blankToNull(adminNickname) == null ? username : adminNickname.trim());
        admin.setEmail(blankToNull(adminEmail));
        admin.setStatus(1);
        admin.setSiteOwner(0);

        if (admin.getId() == null) {
            userService.save(admin);
        } else {
            userService.updateById(admin);
        }
        return admin;
    }

    private void ensureUserRole(User admin, Role adminRole) {
        Long count = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, admin.getId())
                .eq(UserRole::getRoleId, adminRole.getId()));
        if (count != null && count > 0) {
            return;
        }

        UserRole relation = new UserRole();
        relation.setUserId(admin.getId());
        relation.setRoleId(adminRole.getId());
        userRoleMapper.insert(relation);
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
