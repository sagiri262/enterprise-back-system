package org.example.enterprisebacksystem.dto.role;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.enterprisebacksystem.common.api.PageReq;

@Data
@EqualsAndHashCode(callSuper = true)
public class RolePageReq extends PageReq {
    private String keyword;
}
