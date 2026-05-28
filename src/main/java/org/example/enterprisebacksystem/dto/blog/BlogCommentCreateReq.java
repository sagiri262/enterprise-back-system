package org.example.enterprisebacksystem.dto.blog;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BlogCommentCreateReq {
    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    @Size(max = 80, message = "昵称最长80字符")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 120, message = "邮箱最长120字符")
    private String email;

    @NotBlank(message = "评论不能为空")
    @Size(max = 1200, message = "评论最长1200字符")
    private String content;

    @NotBlank(message = "Cloudflare人机验证不能为空")
    private String turnstileToken;

    @NotBlank(message = "验证码ID不能为空")
    private String captchaId;

    @NotBlank(message = "验证码答案不能为空")
    private String captchaAnswer;
}
