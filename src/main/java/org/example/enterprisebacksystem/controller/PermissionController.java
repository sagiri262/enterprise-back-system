package org.example.enterprisebacksystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.domain.Permission;
import org.example.enterprisebacksystem.dto.permission.PermissionSaveReq;
import org.example.enterprisebacksystem.service.PermissionService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    // 分页查询（支持按 name/code 模糊；也可按 type 过滤）
    @GetMapping
    public ApiResponse<Page<Permission>> page(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Integer type) {
        Page<Permission> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(type != null, Permission::getType, type)
                .and(keyword != null && !keyword.isBlank(), w -> w
                        .like(Permission::getName, keyword).or()
                        .like(Permission::getCode, keyword))
                .orderByDesc(Permission::getCreateTime);

        return ApiResponse.ok(permissionService.page(pageParam, wrapper));
    }

    // 新增
    @PostMapping
    public ApiResponse<Boolean> create(@Valid @RequestBody PermissionSaveReq req) {
        Permission p = new Permission();
        BeanUtils.copyProperties(req, p);
        return ApiResponse.ok(permissionService.save(p));
    }

    // 修改
    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody PermissionSaveReq req) {
        Permission p = new Permission();
        BeanUtils.copyProperties(req, p);
        p.setId(id);
        return ApiResponse.ok(permissionService.updateById(p));
    }

    // 删除
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        return ApiResponse.ok(permissionService.removeById(id));
    }
}
