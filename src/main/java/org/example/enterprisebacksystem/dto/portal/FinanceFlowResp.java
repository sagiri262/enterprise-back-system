package org.example.enterprisebacksystem.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FinanceFlowResp {
    private String title;
    private String desc;
    private String steps;
}
