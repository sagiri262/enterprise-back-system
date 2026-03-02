package org.example.enterprisebacksystem.controller;

// 导入 mybatis 、 springboot bean
// jakarta.validation 、 RequiredArgsConstructor
// 导入已经写好的 API、DOMAIN、DTO、SERVICE
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.domain.Role;
import org.example.enterprisebacksystem.dto.role.RoleSaveReq;
import org.example.enterprisebacksystem.service.RoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    // 分页查询
    @GetMapping
    public ApiResponse<Page<Role>> page(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(required = false) String keyword) {
        Page<Role> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(keyword != null && !keyword.isBlank(), Role::getName, keyword)
                .or()
                .like(keyword != null && !keyword.isBlank(), Role::getName, keyword)
                .orderByDesc(Role::getCreateTime);

        return ApiResponse.ok(roleService.page(pageParam, wrapper));
    }

    // 新增
    @PostMapping
    public ApiResponse<Boolean> create(@Valid @RequestBody RoleSaveReq req) {
        Role role = new Role();
        BeanUtils.copyProperties(req, role);
        return ApiResponse.ok(roleService.save(role));
    }

    // 修改
    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody RoleSaveReq req) {
        Role role = new Role();
        BeanUtils.copyProperties(req, role);
        role.setId(id);
        return ApiResponse.ok(roleService.updateById(role));
    }

    // 删除（物理删除；如果你想要逻辑删除，后面我们再加 deleted 字段和 @TableLogic）
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        return ApiResponse.ok(roleService.removeById(id));
    }
}
