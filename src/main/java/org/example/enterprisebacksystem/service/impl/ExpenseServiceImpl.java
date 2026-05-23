package org.example.enterprisebacksystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.enterprisebacksystem.domain.Expense;
import org.example.enterprisebacksystem.mapper.ExpenseMapper;
import org.example.enterprisebacksystem.service.ExpenseService;
import org.springframework.stereotype.Service;

@Service
public class ExpenseServiceImpl extends ServiceImpl<ExpenseMapper, Expense> implements ExpenseService {
}
