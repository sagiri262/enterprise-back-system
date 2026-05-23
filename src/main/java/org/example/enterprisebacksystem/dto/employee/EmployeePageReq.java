package org.example.enterprisebacksystem.dto.employee;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.enterprisebacksystem.common.api.PageReq;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeePageReq extends PageReq {
    private String keyword;
    private Long departmentId;
    private Integer status;
}
