package org.example.enterprisebacksystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.common.annotation.AuditLog;
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.domain.Employee;
import org.example.enterprisebacksystem.dto.employee.EmployeePageReq;
import org.example.enterprisebacksystem.dto.employee.EmployeeSaveReq;
import org.example.enterprisebacksystem.service.EmployeeService;
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
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasAuthority('employee:list')")
    public ApiResponse<Page<Employee>> page(@Valid EmployeePageReq req) {
        Page<Employee> pageParam = new Page<>(req.getPage(), req.getSize());
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(req.getKeyword() != null && !req.getKeyword().isBlank(), w -> w
                        .like(Employee::getName, req.getKeyword()).or()
                        .like(Employee::getEmployeeNo, req.getKeyword()).or()
                        .like(Employee::getPosition, req.getKeyword()))
                .eq(req.getDepartmentId() != null, Employee::getDepartmentId, req.getDepartmentId())
                .eq(req.getStatus() != null, Employee::getStatus, req.getStatus())
                .orderByDesc(Employee::getOnlineStatus)
                .orderByDesc(Employee::getCreateTime);
        return ApiResponse.ok(employeeService.page(pageParam, wrapper));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('employee:create')")
    @AuditLog("新增员工")
    public ApiResponse<Boolean> create(@Valid @RequestBody EmployeeSaveReq req) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(req, employee);
        return ApiResponse.ok(employeeService.save(employee));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('employee:update')")
    @AuditLog("修改员工")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody EmployeeSaveReq req) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(req, employee);
        employee.setId(id);
        return ApiResponse.ok(employeeService.updateById(employee));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('employee:delete')")
    @AuditLog("删除员工")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        return ApiResponse.ok(employeeService.removeById(id));
    }
}
