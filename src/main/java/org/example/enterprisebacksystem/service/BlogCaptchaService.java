package org.example.enterprisebacksystem.service;

import org.example.enterprisebacksystem.dto.blog.BlogCaptchaResp;

public interface BlogCaptchaService {
    BlogCaptchaResp create();

    boolean verify(String id, String answer);
}
