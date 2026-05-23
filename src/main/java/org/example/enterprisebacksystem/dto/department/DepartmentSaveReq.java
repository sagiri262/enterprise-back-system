package org.example.enterprisebacksystem.dto.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentSaveReq {
    private Long parentId = 0L;

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 80, message = "部门名称最长80字符")
    private String name;

    @NotBlank(message = "部门编码不能为空")
    @Size(max = 50, message = "部门编码最长50字符")
    private String code;

    private Long managerEmployeeId;

    @Size(max = 200, message = "描述最长200字符")
    private String description;

    private Integer status = 1;
}
