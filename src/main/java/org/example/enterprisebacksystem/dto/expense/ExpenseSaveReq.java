package org.example.enterprisebacksystem.dto.expense;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExpenseSaveReq {
    @NotBlank(message = "费用标题不能为空")
    @Size(max = 120, message = "费用标题最长120字符")
    private String title;

    @NotBlank(message = "费用分类不能为空")
    @Size(max = 50, message = "费用分类最长50字符")
    private String category;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;

    private Long applicantEmployeeId;
    private Long departmentId;

    @Size(max = 30, message = "状态最长30字符")
    private String status;

    @Size(max = 500, message = "备注最长500字符")
    private String remark;
}
