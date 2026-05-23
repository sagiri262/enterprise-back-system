package org.example.enterprisebacksystem.dto.user;

import org.example.enterprisebacksystem.common.api.PageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserPageReq extends PageReq {
    private String keyword;
    private Integer status;
}
