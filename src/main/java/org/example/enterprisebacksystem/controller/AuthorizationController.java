package org.example.enterprisebacksystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.common.annotation.AuditLog;
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.dto.auth.AssignRolePermissionsReq;
import org.example.enterprisebacksystem.dto.auth.AssignUserRolesReq;
import org.example.enterprisebacksystem.service.AuthorizationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
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