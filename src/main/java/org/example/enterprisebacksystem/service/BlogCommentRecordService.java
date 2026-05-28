package org.example.enterprisebacksystem.service;

import jakarta.servlet.http.HttpServletRequest;
import org.example.enterprisebacksystem.domain.BlogComment;

public interface BlogCommentRecordService {
    void record(BlogComment comment, HttpServletRequest request, boolean loggedIn);
}
