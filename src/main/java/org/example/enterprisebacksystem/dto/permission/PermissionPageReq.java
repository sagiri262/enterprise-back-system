package org.example.enterprisebacksystem.dto.permission;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.enterprisebacksystem.common.api.PageReq;

@Data
@EqualsAndHashCode(callSuper = true)
public class PermissionPageReq extends PageReq {
    private String keyword;
    private Integer type;
}
