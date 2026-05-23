package org.example.enterprisebacksystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.common.annotation.AuditLog;
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.common.api.ErrorCode;
import org.example.enterprisebacksystem.common.enums.PermissionType;
import org.example.enterprisebacksystem.common.exception.BizException;
import org.example.enterprisebacksystem.domain.Permission;
import org.example.enterprisebacksystem.dto.permission.PermissionPageReq;
import org.example.enterprisebacksystem.dto.permission.PermissionSaveReq;
import org.example.enterprisebacksystem.service.PermissionService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    // 分页查询（支持按 name/code 模糊；也可按 type 过滤）
    @GetMapping
    @PreAuthorize("hasAuthority('permission:list')")
    public ApiResponse<Page<Permission>> page(@Valid PermissionPageReq req) {
        Page<Permission> pageParam = new Page<>(req.getPage(), req.getSize());

        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(req.getType() != null, Permission::getType, req.getType())
                .and(req.getKeyword() != null && !req.getKeyword().isBlank(), w -> w
                        .like(Permission::getName, req.getKeyword()).or()
                        .like(Permission::getCode, req.getKeyword()))
                .orderByDesc(Permission::getCreateTime);

        return ApiResponse.ok(permissionService.page(pageParam, wrapper));
    }

    // 新增
    @PostMapping
    @PreAuthorize("hasAuthority('permission:create')")
    @AuditLog("新增权限")
    public ApiResponse<Boolean> create(@Valid @RequestBody PermissionSaveReq req) {
        validateType(req.getType());
        Permission p = new Permission();
        BeanUtils.copyProperties(req, p);
        return ApiResponse.ok(permissionService.save(p));
    }

    // 修改
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:update')")
    @AuditLog("修改权限")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody PermissionSaveReq req) {
        validateType(req.getType());
        Permission p = new Permission();
        BeanUtils.copyProperties(req, p);
        p.setId(id);
        return ApiResponse.ok(permissionService.updateById(p));
    }

    // 删除
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:delete')")
    @AuditLog("删除权限")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        return ApiResponse.ok(permissionService.removeById(id));
    }

    private void validateType(Integer type) {
        if (!PermissionType.isValid(type)) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "权限类型只能是 1 或 2");
        }
    }
}
