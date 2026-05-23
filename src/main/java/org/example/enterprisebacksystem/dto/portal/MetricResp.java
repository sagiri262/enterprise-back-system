package org.example.enterprisebacksystem.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MetricResp {
    private String label;
    private String value;
}
