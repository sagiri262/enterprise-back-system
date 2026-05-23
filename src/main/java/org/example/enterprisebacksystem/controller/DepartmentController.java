package org.example.enterprisebacksystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.common.annotation.AuditLog;
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.domain.Department;
import org.example.enterprisebacksystem.dto.department.DepartmentPageReq;
import org.example.enterprisebacksystem.dto.department.DepartmentSaveReq;
import org.example.enterprisebacksystem.service.DepartmentService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @PreAuthorize("hasAuthority('department:list')")
    public ApiResponse<Page<Department>> page(@Valid DepartmentPageReq req) {
        Page<Department> pageParam = new Page<>(req.getPage(), req.getSize());
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(req.getKeyword() != null && !req.getKeyword().isBlank(), w -> w
                        .like(Department::getName, req.getKeyword()).or()
                        .like(Department::getCode, req.getKeyword()))
                .eq(req.getStatus() != null, Department::getStatus, req.getStatus())
                .orderByAsc(Department::getParentId)
                .orderByDesc(Department::getCreateTime);
        return ApiResponse.ok(departmentService.page(pageParam, wrapper));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('department:create')")
    @AuditLog("新增部门")
    public ApiResponse<Boolean> create(@Valid @RequestBody DepartmentSaveReq req) {
        Department department = new Department();
        BeanUtils.copyProperties(req, department);
        return ApiResponse.ok(departmentService.save(department));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('department:update')")
    @AuditLog("修改部门")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody DepartmentSaveReq req) {
        Department department = new Department();
        BeanUtils.copyProperties(req, department);
        department.setId(id);
        return ApiResponse.ok(departmentService.updateById(department));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('department:delete')")
    @AuditLog("删除部门")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        return ApiResponse.ok(departmentService.removeById(id));
    }
}
