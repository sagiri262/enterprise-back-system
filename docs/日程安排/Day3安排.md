今天的内容分两步走，这一步我们先打通登录接口，下一步我们写拦截器（让没登录的人无法访问接口）。

1、刷新 Maven 依赖，启动项目。
2、打开 Swagger 或 ApiPost/Postman。
3、调用 /api/auth/register-test：传入 JSON {"username":"admin2", "password":"123"}，观察数据库，此时存进去的密码应该是一长串乱码（如 $2a$10$xxxxxx...），这就是 BCrypt 加密后的结果。 
4、调用 /api/auth/login：传入刚才的账号密码。
5、验证：如果一切顺利，你将收到包含长串字母数字的 token 的 JSON 响应！

太棒了！前两天的基础打得很扎实，现在我们进入企业级开发中非常核心、也是面试必问的环节——**认证与授权 (Day3)**。

为了兼顾“好写简历”和“平滑学习”，我们今天采用 **JWT (JSON Web Token) + BCrypt 密码加密** 的主流方案。

> 💡 **避坑指南**：我们今天**先不引入**完整的 `Spring Security`（它太重了，容易因为默认拦截导致你什么接口都调不通）。我们通过 **“拦截器 (Interceptor) + 自定义 JWT 工具类 + 独立 BCrypt 依赖”** 来实现，这既是企业级轻量化微服务的常见做法，也最适合你彻底理解登录的底层逻辑！

---

# 📅 Day3：登录认证 (JWT + BCrypt)

## 1. 补充 pom.xml 依赖

在 `<dependencies>` 中追加以下 3 个依赖：
1. `jjwt`：目前最流行的 JWT 生成与解析库（这里用最新版 0.12.x）。
2. `spring-security-crypto`：只借用它的 `BCryptPasswordEncoder` 加密工具，不引入整个重量级的 Security 框架。

```xml
<!-- JWT 依赖 (目前推荐的 0.12.x 版本组合) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>

<!-- BCrypt 密码加密工具 (轻量级引入) -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
```

---

## 2. 核心工具类开发

### 2.1 JWT 工具类 (JwtUtils)
**文件类型：Class**
路径：`src/main/java/org/example/enterprisebacksystem/common/utils/JwtUtils.java`

```java
package org.example.enterprisebacksystem.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtils {

    // 密钥：必须至少 256 位（32个字符以上）。企业里通常放在 application.yml 中
    private static final String SECRET_STRING = "EnterpriseBackSystem_SecretKey_Must_Be_Long_Enough_666";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());
    
    // 过期时间：7天 (单位：毫秒)
    private static final long EXPIRATION_TIME = 7L * 24 * 60 * 60 * 1000;

    /**
     * 生成 Token
     * @param userId 用户ID
     * @param username 用户名
     */
    public static String generateToken(Long userId, String username) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId) // 自定义载荷（存放不敏感信息）
                .issuedAt(new Date()) // 签发时间
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 过期时间
                .signWith(SECRET_KEY) // 签名
                .compact();
    }

    /**
     * 解析 Token
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

---

## 3. 登录请求与响应 DTO

**文件类型：Class**
路径：`src/main/java/org/example/enterprisebacksystem/dto/auth/LoginReq.java`
```java
package org.example.enterprisebacksystem.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginReq {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
```

**文件类型：Class**
路径：`src/main/java/org/example/enterprisebacksystem/dto/auth/LoginResp.java`
```java
package org.example.enterprisebacksystem.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResp {
    private String token;     // 令牌
    private Long userId;      // 用户ID
    private String username;  // 用户名
}
```

---

## 4. 核心登录逻辑：AuthService & AuthController

我们在 Service 中使用 BCrypt 校验密码。

**文件类型：Interface & Class**
路径：`src/main/java/org/example/enterprisebacksystem/service/AuthService.java`
```java
package org.example.enterprisebacksystem.service;

import org.example.enterprisebacksystem.dto.auth.LoginReq;
import org.example.enterprisebacksystem.dto.auth.LoginResp;

public interface AuthService {
    LoginResp login(LoginReq req);
}
```

路径：`src/main/java/org/example/enterprisebacksystem/service/impl/AuthServiceImpl.java`
```java
package org.example.enterprisebacksystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.common.api.ErrorCode;
import org.example.enterprisebacksystem.common.exception.BizException;
import org.example.enterprisebacksystem.common.utils.JwtUtils;
import org.example.enterprisebacksystem.domain.User;
import org.example.enterprisebacksystem.dto.auth.LoginReq;
import org.example.enterprisebacksystem.dto.auth.LoginResp;
import org.example.enterprisebacksystem.mapper.UserMapper;
import org.example.enterprisebacksystem.service.AuthService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    
    // 初始化 BCrypt 加密器
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginResp login(LoginReq req) {
        // 1. 根据用户名查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername()));

        if (user == null) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "用户名或密码错误");
        }

        // 2. 校验账号状态
        if (user.getStatus() == 0) {
            throw new BizException(ErrorCode.FORBIDDEN.getCode(), "账号已被禁用，请联系管理员");
        }

        // 3. 校验密码 (req.getPassword() 是明文，user.getPassword() 是数据库里的密文)
        // 第一次测试时，如果数据库密码是明文 "123456"，这步会报错。我们在下面提供注册/修改密码的代码来生成密文。
        boolean matches = passwordEncoder.matches(req.getPassword(), user.getPassword());
        if (!matches) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "用户名或密码错误");
        }

        // 4. 密码正确，生成 JWT Token
        String token = JwtUtils.generateToken(user.getId(), user.getUsername());

        // 5. 返回结果
        return new LoginResp(token, user.getId(), user.getUsername());
    }
}
```

**文件类型：Class**
路径：`src/main/java/org/example/enterprisebacksystem/controller/AuthController.java`
```java
package org.example.enterprisebacksystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.dto.auth.LoginReq;
import org.example.enterprisebacksystem.dto.auth.LoginResp;
import org.example.enterprisebacksystem.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResp> login(@Valid @RequestBody LoginReq req) {
        return ApiResponse.ok(authService.login(req));
    }
}
```

---

## 5. 如何制造一个带 BCrypt 密码的用户？

因为我们开始使用了 BCrypt，之前你数据库里如果是明文密码 `123456`，现在登录会提示“密码错误”。

你可以临时写一个接口，用于给数据库塞入正确的密文密码。
把这段代码加到你的 **`AuthController`** 里面：

```java
import org.example.enterprisebacksystem.domain.User;
import org.example.enterprisebacksystem.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// ... AuthController 类内部追加这个方法（用于测试生成用户） ...
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
```

---

## ✅ Day3 第一阶段测试步骤

今天的内容分两步走，这一步我们先打通登录接口，下一步我们写拦截器（让没登录的人无法访问接口）。

1. 刷新 Maven 依赖，启动项目。
2. 打开 Swagger 或 ApiPost/Postman。
3. **调用 `/api/auth/register-test`**：传入 JSON `{"username":"admin2", "password":"123"}`，观察数据库，此时存进去的密码应该是一长串乱码（如 `$2a$10$xxxxxx...`），这就是 BCrypt 加密后的结果。
4. **调用 `/api/auth/login`**：传入刚才的账号密码。
5. **验证**：如果一切顺利，你将收到包含长串字母数字的 `token` 的 JSON 响应！

**你先跑通这一步！跑通后告诉我，我马上把“JWT全站拦截器（白名单/登录校验）”的代码发给你，形成闭环！**