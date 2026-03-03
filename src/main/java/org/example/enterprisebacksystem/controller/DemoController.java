package org.example.enterprisebacksystem.controller;

import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    // 只有拥有 'user:list' 权限的人才能访问
    @GetMapping("/data")
    @PreAuthorize("hasAuthority('user:list')")
    public ApiResponse<Map<String, Object>> getData() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> result = new HashMap<>();
        result.put("commonInfo", "这是一条所有人都能看到的基础数据");

        // 实现：不同角色结果不同
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_admin"))) {
            result.put("adminSecret", "【特权数据】管理员专用：公司今年要上市！");
        } else {
            result.put("adminSecret", "【提示】你不是管理员，无法查看特权数据");
        }

        return ApiResponse.ok(result);
    }
}
