package org.example.enterprisebacksystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.common.annotation.AuditLog;
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.common.exception.BizException;
import org.example.enterprisebacksystem.domain.BlogArticle;
import org.example.enterprisebacksystem.domain.BlogComment;
import org.example.enterprisebacksystem.dto.blog.BlogCaptchaResp;
import org.example.enterprisebacksystem.dto.blog.BlogCommentCreateReq;
import org.example.enterprisebacksystem.security.LoginUser;
import org.example.enterprisebacksystem.service.BlogArticleService;
import org.example.enterprisebacksystem.service.BlogCaptchaService;
import org.example.enterprisebacksystem.service.BlogCommentRecordService;
import org.example.enterprisebacksystem.service.BlogCommentService;
import org.example.enterprisebacksystem.service.TurnstileService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/blog/comments")
@RequiredArgsConstructor
public class BlogCommentController {
    private final BlogArticleService blogArticleService;
    private final BlogCommentService blogCommentService;
    private final BlogCaptchaService blogCaptchaService;
    private final TurnstileService turnstileService;
    private final BlogCommentRecordService blogCommentRecordService;

    @GetMapping("/captcha")
    public ApiResponse<BlogCaptchaResp> captcha() {
        return ApiResponse.ok(blogCaptchaService.create());
    }

    @GetMapping
    public ApiResponse<List<BlogComment>> list(@RequestParam Long articleId) {
        LambdaQueryWrapper<BlogComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogComment::getArticleId, articleId)
                .eq(BlogComment::getStatus, 1)
                .orderByDesc(BlogComment::getCreateTime);
        return ApiResponse.ok(blogCommentService.list(wrapper));
    }

    @PostMapping
    public ApiResponse<Boolean> create(@Valid @RequestBody BlogCommentCreateReq req, HttpServletRequest request) {
        if (!blogCaptchaService.verify(req.getCaptchaId(), req.getCaptchaAnswer())) {
            throw new BizException(40000, "验证码错误或已过期");
        }
        String ip = clientIp(request);
        if (!turnstileService.verify(req.getTurnstileToken(), ip)) {
            throw new BizException(40000, "Cloudflare人机验证失败");
        }

        BlogArticle article = blogArticleService.getById(req.getArticleId());
        if (article == null || article.getStatus() == null || article.getStatus() != 1) {
            throw new BizException(40400, "文章不存在");
        }

        LoginUser loginUser = currentLoginUser();
        BlogComment comment = new BlogComment();
        comment.setArticleId(req.getArticleId());
        comment.setUserId(loginUser == null ? null : loginUser.getUserId());
        comment.setNickname(resolveNickname(req.getNickname(), loginUser));
        comment.setEmail(resolveEmail(req.getEmail(), loginUser));
        comment.setContent(req.getContent());
        comment.setIpAddress(ip);
        comment.setStatus(1);

        boolean saved = blogCommentService.save(comment);
        BlogArticle update = new BlogArticle();
        update.setId(req.getArticleId());
        update.setCommentCount((article.getCommentCount() == null ? 0 : article.getCommentCount()) + 1);
        blogArticleService.updateById(update);
        blogCommentRecordService.record(comment, request, loginUser != null);
        return ApiResponse.ok(saved);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('blog:comment:delete') or hasRole('admin')")
    @AuditLog("删除博客评论")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        BlogComment comment = blogCommentService.getById(id);
        if (comment == null) {
            return ApiResponse.ok(true);
        }
        boolean removed = blogCommentService.removeById(id);
        BlogArticle article = blogArticleService.getById(comment.getArticleId());
        if (article != null) {
            BlogArticle update = new BlogArticle();
            update.setId(article.getId());
            update.setCommentCount(Math.max(0, (article.getCommentCount() == null ? 0 : article.getCommentCount()) - 1));
            blogArticleService.updateById(update);
        }
        return ApiResponse.ok(removed);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("CF-Connecting-IP");
        if (forwarded == null || forwarded.isBlank()) {
            forwarded = request.getHeader("X-Forwarded-For");
        }
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private LoginUser currentLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    private String resolveNickname(String reqNickname, LoginUser loginUser) {
        if (reqNickname != null && !reqNickname.isBlank()) {
            return reqNickname.trim();
        }
        if (loginUser != null && loginUser.getUser() != null) {
            String nickname = loginUser.getUser().getNickname();
            if (nickname != null && !nickname.isBlank()) {
                return nickname;
            }
            return loginUser.getUsername();
        }
        return "访客";
    }

    private String resolveEmail(String reqEmail, LoginUser loginUser) {
        if (reqEmail != null && !reqEmail.isBlank()) {
            return reqEmail.trim();
        }
        if (loginUser != null && loginUser.getUser() != null) {
            return loginUser.getUser().getEmail();
        }
        return null;
    }
}
