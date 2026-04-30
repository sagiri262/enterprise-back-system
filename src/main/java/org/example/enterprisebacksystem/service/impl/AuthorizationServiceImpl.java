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

    @Transactional(rollbackFor = Exception.class)
    @Override
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

    // @Override 会报错
    // 回头研究一下
    @Transactional(rollbackFor = Exception.class)
    @Override
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

    @Override
    public void assignRoleToUser(Long userId, List<Long> roleIds) {

    }

    @Override
    public void assignPermissionToUser(Long userId, List<Long> permissionIds) {

    }
}