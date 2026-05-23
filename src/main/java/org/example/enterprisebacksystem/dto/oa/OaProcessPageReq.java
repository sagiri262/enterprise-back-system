package org.example.enterprisebacksystem.dto.oa;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.enterprisebacksystem.common.api.PageReq;

@Data
@EqualsAndHashCode(callSuper = true)
public class OaProcessPageReq extends PageReq {
    private String keyword;
    private String category;
    private Integer status;
}
