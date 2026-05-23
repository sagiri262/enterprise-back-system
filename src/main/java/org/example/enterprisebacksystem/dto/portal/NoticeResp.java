package org.example.enterprisebacksystem.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NoticeResp {
    private String title;
    private String desc;
    private String level;
}
