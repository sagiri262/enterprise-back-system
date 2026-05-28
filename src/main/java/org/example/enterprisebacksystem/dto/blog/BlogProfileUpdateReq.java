package org.example.enterprisebacksystem.dto.blog;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BlogProfileUpdateReq {
    @Size(max = 50, message = "昵称最长50字符")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱最长100字符")
    private String email;

    @Size(max = 1048576, message = "头像数据最长1MB")
    private String avatar;

    @Size(max = 300, message = "个人简介最长300字符")
    private String bio;
}
