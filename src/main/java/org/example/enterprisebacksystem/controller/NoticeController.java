package org.example.enterprisebacksystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.common.annotation.AuditLog;
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.domain.Notice;
import org.example.enterprisebacksystem.dto.notice.NoticePageReq;
import org.example.enterprisebacksystem.dto.notice.NoticeSaveReq;
import org.example.enterprisebacksystem.service.NoticeService;
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
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    @PreAuthorize("hasAuthority('notice:list')")
    public ApiResponse<Page<Notice>> page(@Valid NoticePageReq req) {
        Page<Notice> pageParam = new Page<>(req.getPage(), req.getSize());
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(req.getKeyword() != null && !req.getKeyword().isBlank(), w -> w
                        .like(Notice::getTitle, req.getKeyword()).or()
                        .like(Notice::getContent, req.getKeyword()))
                .eq(req.getCategory() != null && !req.getCategory().isBlank(), Notice::getCategory, req.getCategory())
                .eq(req.getStatus() != null, Notice::getStatus, req.getStatus())
                .orderByDesc(Notice::getPublishTime)
                .orderByDesc(Notice::getCreateTime);
        return ApiResponse.ok(noticeService.page(pageParam, wrapper));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('notice:create')")
    @AuditLog("新增消息")
    public ApiResponse<Boolean> create(@Valid @RequestBody NoticeSaveReq req) {
        Notice notice = new Notice();
        BeanUtils.copyProperties(req, notice);
        notice.setPublishTime(LocalDateTime.now());
        return ApiResponse.ok(noticeService.save(notice));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('notice:update')")
    @AuditLog("修改消息")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody NoticeSaveReq req) {
        Notice notice = new Notice();
        BeanUtils.copyProperties(req, notice);
        notice.setId(id);
        return ApiResponse.ok(noticeService.updateById(notice));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('notice:delete')")
    @AuditLog("删除消息")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        return ApiResponse.ok(noticeService.removeById(id));
    }
}
