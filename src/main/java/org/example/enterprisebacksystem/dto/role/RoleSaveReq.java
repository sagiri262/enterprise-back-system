package org.example.enterprisebacksystem.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleSaveReq {

    @NotBlank(message = "角色名不能为空")
    @Size(max = 50, message = "角色名最长50字符")
    private String name;

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码最长50字符")
    private String code;

    @Size(max = 200, message = "描述最长200字符")
    private String description;
}
