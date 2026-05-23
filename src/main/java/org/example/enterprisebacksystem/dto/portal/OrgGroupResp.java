package org.example.enterprisebacksystem.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrgGroupResp {
    private String name;
    private List<OrgPersonResp> people;
}
