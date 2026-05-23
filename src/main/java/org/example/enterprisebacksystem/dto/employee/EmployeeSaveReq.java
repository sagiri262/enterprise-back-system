package org.example.enterprisebacksystem.dto.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmployeeSaveReq {
    private Long userId;

    @NotNull(message = "部门ID不能为空")
    private Long departmentId;

    @NotBlank(message = "员工编号不能为空")
    @Size(max = 50, message = "员工编号最长50字符")
    private String employeeNo;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名最长50字符")
    private String name;

    @Size(max = 80, message = "职位最长80字符")
    private String position;

    @Size(max = 30, message = "层级最长30字符")
    private String level;

    @Size(max = 30, message = "手机号最长30字符")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱最长100字符")
    private String email;

    private Integer contactLevel = 1;
    private Integer onlineStatus = 0;
    private Integer status = 1;
}
