package org.example.enterprisebacksystem.dto.blog;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BlogRegisterReq {
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名最长50字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度应为6到50字符")
    private String password;

    @Size(max = 50, message = "昵称最长50字符")
    private String nickname;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱最长100字符")
    private String email;

    @NotBlank(message = "Cloudflare人机验证不能为空")
    private String turnstileToken;

    @NotBlank(message = "验证码ID不能为空")
    private String captchaId;

    @NotBlank(message = "验证码答案不能为空")
    private String captchaAnswer;
}
