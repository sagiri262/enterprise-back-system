package org.example.enterprisebacksystem.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WorkAppResp {
    private String title;
    private String status;
    private String owner;
    private String tone;
}
