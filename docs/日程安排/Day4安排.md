太棒了！前三天我们完成了骨架搭建、数据库增删改查以及基于 JWT 的基础登录逻辑。**Day 4 是整个项目的“含金量”分水岭**，我们将正式引入 **Spring Security**，将之前的零散逻辑整合进标准的安全框架中，实现**企业级**的认证（Authentication）与授权（Authorization）。

---

# 📅 Day4：Spring Security 深度整合与动态权限

## 1. 补充 pom.xml 依赖

在 `<dependencies>` 中追加 Spring Security 核心起步依赖：

```xml
<!-- Spring Security 核心 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

---

## 2. 权限数据访问层增强

为了让 Security 知道用户有哪些权限，我们需要在 `UserMapper` 或 `PermissionMapper` 中增加查询逻辑。

**路径：** `src/main/java/org/example/enterprisebacksystem/mapper/PermissionMapper.java`

```java
package org.example.enterprisebacksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.example.enterprisebacksystem.domain.Permission;
import java.util.List;

public interface PermissionMapper extends BaseMapper<Permission> {

    // 根据用户 ID 查询权限编码列表 (如: "user:list", "user:add")
    @Select("SELECT DISTINCT p.code " +
            "FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.code IS NOT NULL")
    List<String> selectPermissionCodesByUserId(Long userId);

    // 根据用户 ID 查询角色编码列表 (如: "ROLE_ADMIN")
    @Select("SELECT DISTINCT r.code " +
            "FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> selectRoleCodesByUserId(Long userId);
}
```

---

## 3. Security 核心适配：UserDetails 与 UserDetailsService

Spring Security 不直接操作我们的 `User` 实体，需要通过 `UserDetails` 中转。

### 3.1 定义 LoginUser
**路径：** `src/main/java/org/example/enterprisebacksystem/security/LoginUser.java`

```java
package org.example.enterprisebacksystem.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enterprisebacksystem.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements UserDetails {

    private User user;
    private List<String> permissions; // 权限 code 列表

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 将字符串权限转换为 Security 要求的 GrantedAuthority 对象
        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() { return user.getPassword(); }
    @Override
    public String getUsername() { return user.getUsername(); }
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return user.getStatus() == 1; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}
```

### 3.2 实现 UserDetailsService
**路径：** `src/main/java/org/example/enterprisebacksystem/security/UserDetailsServiceImpl.java`

```java
package org.example.enterprisebacksystem.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.domain.User;
import org.example.enterprisebacksystem.mapper.UserMapper;
import org.example.enterprisebacksystem.mapper.PermissionMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final PermissionMapper permissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 查询用户信息
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) throw new UsernameNotFoundException("用户不存在");

        // 2. 查询权限和角色（Spring Security 角色建议加 ROLE_ 前缀）
        List<String> perms = permissionMapper.selectPermissionCodesByUserId(user.getId());
        List<String> roles = permissionMapper.selectRoleCodesByUserId(user.getId());
        
        List<String> allAuthorities = new ArrayList<>(perms);
        roles.forEach(role -> allAuthorities.add("ROLE_" + role));

        return new LoginUser(user, allAuthorities);
    }
}
```

---

## 4. 核心：JWT 认证过滤器

我们需要一个过滤器，在每个请求进来时，解析 Token 并告知 Security “这个人是谁”。

**路径：** `src/main/java/org/example/enterprisebacksystem/security/JwtAuthenticationTokenFilter.java`

```java
package org.example.enterprisebacksystem.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.common.utils.JwtUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        // 1. 获取 token (从 Header: Authorization 中获取)
        String token = request.getHeader("Authorization");
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. 解析 token
            Claims claims = JwtUtils.parseToken(token);
            String username = claims.getSubject();

            // 3. 封装 Authentication 对象存入上下文中
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authenticationToken = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        } catch (Exception e) {
            // Token 无效或过期，这里暂不处理，交给 Security 后续过滤器拦截
        }

        filterChain.doFilter(request, response);
    }
}
```

---

## 5. Security 全局配置

**路径：** `src/main/java/org/example/enterprisebacksystem/config/SecurityConfig.java`

```java
package org.example.enterprisebacksystem.config;

import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.security.JwtAuthenticationTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 开启接口权限注解，如 @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // 禁用 CSRF（前后端分离不需要）
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 禁用 Session
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll() // 白名单
                .anyRequest().authenticated() // 其余接口必须登录
            );

        // 将我们的 JWT 过滤器放在系统账号密码过滤器之前
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

---

## 6. 验证：不同角色，结果不同

我们修改 `DemoController`，演示如何通过注解控制权限，以及在代码里区分角色返回数据。

**路径：** `src/main/java/org/example/enterprisebacksystem/controller/DemoController.java`

```java
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
```

---

## ✅ Day4 验收清单

1.  **重启项目**：检查控制台是否有 Security 注入报错。
2.  **测试白名单**：访问 `http://localhost:8080/swagger-ui.html`，应能直接打开。
3.  **测试认证拦截**：直接调用 `GET /api/demo/data`（不带 Token），应返回 `403` 或 `401`。
4.  **测试权限校验**：
    *   登录 `admin2` 账号获取 Token。
    *   在 Header 中加入 `Authorization: [你的Token]` 调用接口。
    *   **效果 1**：如果数据库里该用户没有 `user:list` 权限，返回 403。
    *   **效果 2**：如果有 `user:list` 权限但没有 `admin` 角色，看到普通结果。
    *   **效果 3**：如果既有权限又是 `admin` 角色，看到包含“特权数据”的结果。

**至此，你的项目已经具备了完整的权限防线！接下来我们可以开始做 Day 5：AOP 审计日志与系统监控了。有什么需要我解释的代码细节吗？**

经过前四天的整合，你的启动类（`EnterpriseBackSystemApplication.java`）不仅是程序的入口，还承担了**组件扫描**、**事务管理**、以及**性能监控（如 SQL 日志）**的配置开启。

根据你目前采用的包名 `org.example.enterprisebacksystem`，启动类的完整代码如下：

### 1. 启动类代码实现

**路径：** `src/main/java/org/example/enterprisebacksystem/EnterpriseBackSystemApplication.java`

```java
package org.example.enterprisebacksystem;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@SpringBootApplication
@EnableTransactionManagement // 开启声明式事务支持，Day2 业务代码必须用到
public class EnterpriseBackSystemApplication {

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext application = SpringApplication.run(EnterpriseBackSystemApplication.class, args);
        
        // 打印启动成功的漂亮日志，方便直接点击链接
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path", "");
        
        log.info("\n----------------------------------------------------------\n\t" +
                "Application EnterpriseBackSystem is running! Access URLs:\n\t" +
                "Local: \t\thttp://localhost:" + port + path + "/\n\t" +
                "External: \thttp://" + ip + ":" + port + path + "/\n\t" +
                "Swagger: \thttp://localhost:" + port + path + "/swagger-ui.html\n" +
                "----------------------------------------------------------");
    }
}
```

---

### 2. 核心细节说明（为什么这么写）

1.  **`@EnableTransactionManagement`**：
    你在 Day 2 编写了大量的 CRUD 代码。在企业开发中，比如“新增用户并同时分配角色”需要操作两张表，必须保证原子性。这个注解确保了你在 Service 层方法上标注 `@Transactional` 时，事务能真正生效。

2.  **`@Slf4j` 与启动日志打印**：
    这不仅是为了美观。在实际开发中，你可能经常忘记端口号或者 Swagger 的具体路径。代码中的 `log.info` 会在启动完成后直接输出可点击的 URL 链接，大幅提升调试效率。

3.  **包扫描范围**：
    由于启动类放在根包 `org.example.enterprisebacksystem` 下，Spring Boot 默认会扫描该包及其子包下的所有组件：
    *   `controller/` (Day 1)
    *   `service/` (Day 2/3)
    *   `config/` (SecurityConfig, MybatisPlusConfig 等)
    *   `security/` (UserDetailsServiceImpl, Filter 等)

### 3. 特别注意（关于 Mapper 扫描）

我在 Day 2 的 `MybatisPlusConfig.java` 中已经为你写了 `@MapperScan("org.example.enterprisebacksystem.mapper")`。

*   **如果你的 `MybatisPlusConfig` 已经有了这个注解**：启动类就保持上面这样。
*   **如果你想让启动类更整洁**：也可以把 `@MapperScan` 从 Config 类移到这个启动类上面。

### ✅ 启动前的最后自检
1.  **数据库连接**：确保 `application.yml` 里的数据库账号密码正确。
2.  **依赖刷新**：由于新增了 Spring Security 依赖，请务必在 IDE 中执行 `Reload All Maven Projects`。
3.  **运行**：直接点击启动类旁边的绿色箭头，如果控制台输出了上面的 `----------------` 框框且没有红色报错，说明你这四天的工程基座已经彻底打稳了！

**目前项目已经跑起来了吗？如果启动报错，请把报错日志贴给我，我来帮你定位。**