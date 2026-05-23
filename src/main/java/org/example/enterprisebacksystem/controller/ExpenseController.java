package org.example.enterprisebacksystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.common.annotation.AuditLog;
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.domain.Expense;
import org.example.enterprisebacksystem.dto.expense.ExpensePageReq;
import org.example.enterprisebacksystem.dto.expense.ExpenseSaveReq;
import org.example.enterprisebacksystem.service.ExpenseService;
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
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    @PreAuthorize("hasAuthority('expense:list')")
    public ApiResponse<Page<Expense>> page(@Valid ExpensePageReq req) {
        Page<Expense> pageParam = new Page<>(req.getPage(), req.getSize());
        LambdaQueryWrapper<Expense> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(req.getKeyword() != null && !req.getKeyword().isBlank(), w -> w
                        .like(Expense::getTitle, req.getKeyword()).or()
                        .like(Expense::getRemark, req.getKeyword()))
                .eq(req.getCategory() != null && !req.getCategory().isBlank(), Expense::getCategory, req.getCategory())
                .eq(req.getStatus() != null && !req.getStatus().isBlank(), Expense::getStatus, req.getStatus())
                .orderByDesc(Expense::getCreateTime);
        return ApiResponse.ok(expenseService.page(pageParam, wrapper));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('expense:create')")
    @AuditLog("新增费用")
    public ApiResponse<Boolean> create(@Valid @RequestBody ExpenseSaveReq req) {
        Expense expense = new Expense();
        BeanUtils.copyProperties(req, expense);
        return ApiResponse.ok(expenseService.save(expense));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('expense:update')")
    @AuditLog("修改费用")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody ExpenseSaveReq req) {
        Expense expense = new Expense();
        BeanUtils.copyProperties(req, expense);
        expense.setId(id);
        return ApiResponse.ok(expenseService.updateById(expense));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('expense:delete')")
    @AuditLog("删除费用")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        return ApiResponse.ok(expenseService.removeById(id));
    }
}
