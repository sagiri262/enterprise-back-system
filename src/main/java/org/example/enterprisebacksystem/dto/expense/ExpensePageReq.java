package org.example.enterprisebacksystem.dto.expense;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.enterprisebacksystem.common.api.PageReq;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExpensePageReq extends PageReq {
    private String keyword;
    private String category;
    private String status;
}
