package org.example.enterprisebacksystem.dto.auth;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class AssignRolePermissionsReq {
    @NotEmpty(message = "权限 ID 列表不能为空")
    private List<Long> permissionIds;
}
