package org.example.enterprisebacksystem.dto.blog;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BlogCaptchaResp {
    private String id;
    private String question;
    private String image;
}
