package org.example.enterprisebacksystem.dto.notice;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.enterprisebacksystem.common.api.PageReq;

@Data
@EqualsAndHashCode(callSuper = true)
public class NoticePageReq extends PageReq {
    private String keyword;
    private String category;
    private Integer status;
}
