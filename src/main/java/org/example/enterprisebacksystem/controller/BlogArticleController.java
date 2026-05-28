package org.example.enterprisebacksystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.common.annotation.AuditLog;
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.common.api.ErrorCode;
import org.example.enterprisebacksystem.common.exception.BizException;
import org.example.enterprisebacksystem.domain.BlogArticle;
import org.example.enterprisebacksystem.domain.BlogComment;
import org.example.enterprisebacksystem.dto.blog.BlogArticlePageReq;
import org.example.enterprisebacksystem.dto.blog.BlogArticleSaveReq;
import org.example.enterprisebacksystem.service.BlogArticleService;
import org.example.enterprisebacksystem.service.BlogCommentService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/blog/articles")
@RequiredArgsConstructor
public class BlogArticleController {
    private final BlogArticleService blogArticleService;
    private final BlogCommentService blogCommentService;

    @GetMapping
    public ApiResponse<Page<BlogArticle>> page(@Valid BlogArticlePageReq req) {
        return ApiResponse.ok(pageArticles(req, true));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('blog:article:list') or hasRole('admin')")
    public ApiResponse<Page<BlogArticle>> adminPage(@Valid BlogArticlePageReq req) {
        return ApiResponse.ok(pageArticles(req, false));
    }

    private Page<BlogArticle> pageArticles(BlogArticlePageReq req, boolean onlyPublished) {
        Page<BlogArticle> pageParam = new Page<>(req.getPage(), req.getSize());
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(onlyPublished, BlogArticle::getStatus, 1)
                .like(req.getKeyword() != null && !req.getKeyword().isBlank(), BlogArticle::getTitle, req.getKeyword())
                .orderByDesc(BlogArticle::getPublishTime)
                .orderByDesc(BlogArticle::getCreateTime);
        return blogArticleService.page(pageParam, wrapper);
    }

    @GetMapping("/{id}")
    public ApiResponse<BlogArticle> detail(@PathVariable Long id) {
        BlogArticle article = blogArticleService.getById(id);
        if (article == null || article.getStatus() == null || article.getStatus() != 1) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        BlogArticle update = new BlogArticle();
        update.setId(id);
        update.setReadCount((article.getReadCount() == null ? 0 : article.getReadCount()) + 1);
        blogArticleService.updateById(update);
        article.setReadCount(update.getReadCount());
        return ApiResponse.ok(article);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('blog:article:create') or hasRole('admin')")
    @AuditLog("新增博客文章")
    public ApiResponse<Boolean> create(@Valid @RequestBody BlogArticleSaveReq req) {
        BlogArticle article = new BlogArticle();
        BeanUtils.copyProperties(req, article);
        article.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        article.setReadCount(0);
        article.setCommentCount(0);
        article.setPublishTime(LocalDateTime.now());
        return ApiResponse.ok(blogArticleService.save(article));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('blog:article:update') or hasRole('admin')")
    @AuditLog("修改博客文章")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody BlogArticleSaveReq req) {
        BlogArticle article = new BlogArticle();
        BeanUtils.copyProperties(req, article);
        article.setId(id);
        article.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        return ApiResponse.ok(blogArticleService.updateById(article));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('blog:article:delete') or hasRole('admin')")
    @AuditLog("删除博客文章")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        blogCommentService.remove(new LambdaQueryWrapper<BlogComment>().eq(BlogComment::getArticleId, id));
        return ApiResponse.ok(blogArticleService.removeById(id));
    }
}
