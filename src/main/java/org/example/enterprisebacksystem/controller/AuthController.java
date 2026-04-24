package org.example.enterprisebacksystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
// 本地文件
import org.example.enterprisebacksystem.common.annotation.AuditLog;
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.domain.User;
import org.example.enterprisebacksystem.dto.auth.LoginReq;
import org.example.enterprisebacksystem.dto.auth.LoginResp;
import org.example.enterprisebacksystem.service.AuthService;
import org.example.enterprisebacksystem.service.UserService;
// Spring Boot 包
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/common/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResp> login(@Valid @RequestBody LoginReq req) {
        return ApiResponse.ok(authService.login(req));
    }

    /*
     * 开发测试用：生成 BCrypt 密码用户。
     * 后续正式项目可以删除或只在 dev 环境开放。
     */
    /*
    *
    *     @PostMapping("/register-test")
    public ApiResponse<String> registerTest(@Valid @RequestBody LoginReq req) {
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname("测试用户");
        user.setStatus(1);
        user.setDeleted(0);

        userService.save(user);
        return ApiResponse.ok("注册成功");
    }*/

    /*
    *
    * // ... AuthController 类内部追加这个方法（用于测试生成用户） ...
        private final UserService userService; // 记得在类上面加上这个依赖，或者用 @RequiredArgsConstructor 注入

        @PostMapping("/register-test")
        public ApiResponse<String> registerTest(@RequestBody LoginReq req) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            User user = new User();
            user.setUsername(req.getUsername());
            // 自动将明文密码加密成 BCrypt 密文存入数据库
            user.setPassword(encoder.encode(req.getPassword()));
            user.setNickname("测试用户");
            user.setStatus(1);
            userService.save(user); // 需确保引入了 UserService
            return ApiResponse.ok("注册成功，快去测试登录吧！");
        }
    * */
}
