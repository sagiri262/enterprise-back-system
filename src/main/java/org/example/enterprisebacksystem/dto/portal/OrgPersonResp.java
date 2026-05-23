package org.example.enterprisebacksystem.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrgPersonResp {
    private String name;
    private String role;
    private String contact;
    private Boolean locked;
}
