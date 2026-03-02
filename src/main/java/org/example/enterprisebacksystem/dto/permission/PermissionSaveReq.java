package org.example.enterprisebacksystem.dto.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PermissionSaveReq {
    @NotNull(message = "parentId 不能为空")
    private Long parentId;

    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称最长50字符")
    private String name;

    @Size(max = 100, message = "权限标识最长100字符")
    private String code;

    @NotNull(message = "type 不能为空（1菜单/2按钮）")
    private Integer type;

    @Size(max = 200, message = "path 最长200字符")
    private String path;
}
