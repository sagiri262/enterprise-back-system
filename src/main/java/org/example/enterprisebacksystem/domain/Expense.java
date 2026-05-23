package org.example.enterprisebacksystem.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_expense")
public class Expense extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String category;
    private BigDecimal amount;
    private Long applicantEmployeeId;
    private Long departmentId;
    private String status;
    private String remark;
}
