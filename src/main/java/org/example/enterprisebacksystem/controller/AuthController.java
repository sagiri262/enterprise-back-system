package org.example.enterprisebacksystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
// 本地文件
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.dto.auth.LoginReq;
import org.example.enterprisebacksystem.dto.auth.LoginResp;
import org.example.enterprisebacksystem.service.AuthService;
// Spring Boot 包
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
