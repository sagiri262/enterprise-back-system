package org.example.enterprisebacksystem.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enterprisebacksystem.domain.BlogComment;
import org.example.enterprisebacksystem.service.BlogCommentRecordService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogCommentRecordServiceImpl implements BlogCommentRecordService {
    private final ObjectMapper objectMapper;

    @Value("${blog.comment-record.path:logs/blog-comment-records.jsonl}")
    private String recordPath;

    @Override
    public void record(BlogComment comment, HttpServletRequest request, boolean loggedIn) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("commentId", comment.getId());
        item.put("articleId", comment.getArticleId());
        item.put("userId", comment.getUserId());
        item.put("nickname", comment.getNickname());
        item.put("loggedIn", loggedIn);
        item.put("ipAddress", comment.getIpAddress());
        LocalDateTime commentTime = comment.getCreateTime() == null ? LocalDateTime.now() : comment.getCreateTime();
        item.put("commentTime", commentTime.toString());
        item.put("path", request.getRequestURI());

        try {
            Path path = Paths.get(recordPath);
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(
                    path,
                    objectMapper.writeValueAsString(item) + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException ex) {
            log.warn("写入博客评论记录文件失败", ex);
        }
    }
}
