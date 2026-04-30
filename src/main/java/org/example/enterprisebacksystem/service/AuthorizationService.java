package org.example.enterprisebacksystem.service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AuthorizationService {
    @Transactional(rollbackFor = Exception.class)
    void assignRolesToUser(Long userId, List<Long> roleIds);

    @Transactional(rollbackFor = Exception.class)
    void assignPermissionsToRole(Long roleId, List<Long> permissionIds);

    void assignRoleToUser(Long userId, List<Long> roleIds);

    void assignPermissionToUser(Long userId, List<Long> permissionIds);
}
