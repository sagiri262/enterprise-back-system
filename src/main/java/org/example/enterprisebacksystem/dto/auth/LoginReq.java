package org.example.enterprisebacksystem.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class LoginReq {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "用户密码不能为空")
    private String password;
}
