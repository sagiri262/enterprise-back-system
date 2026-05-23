package org.example.enterprisebacksystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.common.annotation.AuditLog;
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.domain.OaProcess;
import org.example.enterprisebacksystem.dto.oa.OaProcessPageReq;
import org.example.enterprisebacksystem.dto.oa.OaProcessSaveReq;
import org.example.enterprisebacksystem.service.OaProcessService;
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

@RestController
@RequestMapping("/api/oa-processes")
@RequiredArgsConstructor
public class OaProcessController {

    private final OaProcessService oaProcessService;

    @GetMapping
    @PreAuthorize("hasAuthority('oa:list')")
    public ApiResponse<Page<OaProcess>> page(@Valid OaProcessPageReq req) {
        Page<OaProcess> pageParam = new Page<>(req.getPage(), req.getSize());
        LambdaQueryWrapper<OaProcess> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(req.getKeyword() != null && !req.getKeyword().isBlank(), w -> w
                        .like(OaProcess::getTitle, req.getKeyword()).or()
                        .like(OaProcess::getCode, req.getKeyword()))
                .eq(req.getCategory() != null && !req.getCategory().isBlank(), OaProcess::getCategory, req.getCategory())
                .eq(req.getStatus() != null, OaProcess::getStatus, req.getStatus())
                .orderByDesc(OaProcess::getPendingCount)
                .orderByDesc(OaProcess::getCreateTime);
        return ApiResponse.ok(oaProcessService.page(pageParam, wrapper));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('oa:create')")
    @AuditLog("新增OA流程")
    public ApiResponse<Boolean> create(@Valid @RequestBody OaProcessSaveReq req) {
        OaProcess process = new OaProcess();
        BeanUtils.copyProperties(req, process);
        return ApiResponse.ok(oaProcessService.save(process));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('oa:update')")
    @AuditLog("修改OA流程")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody OaProcessSaveReq req) {
        OaProcess process = new OaProcess();
        BeanUtils.copyProperties(req, process);
        process.setId(id);
        return ApiResponse.ok(oaProcessService.updateById(process));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('oa:delete')")
    @AuditLog("删除OA流程")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        return ApiResponse.ok(oaProcessService.removeById(id));
    }
}
