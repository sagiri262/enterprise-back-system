package org.example.enterprisebacksystem.dto.department;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.enterprisebacksystem.common.api.PageReq;

@Data
@EqualsAndHashCode(callSuper = true)
public class DepartmentPageReq extends PageReq {
    private String keyword;
    private Integer status;
}
