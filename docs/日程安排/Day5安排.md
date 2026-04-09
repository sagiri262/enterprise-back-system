太棒了！**Day 5 是项目“企业级工程化”的收官之作**。通过 **AOP (面向切面编程)** 实现审计日志，你不仅能展示对 Spring 核心原理的理解，还能为项目增加“操作追踪”和“性能监控”的硬核功能。

以下是 Day 5 的完整代码实现，以及你要求的最终启动类整合。

---

# 📅 Day 5：AOP 审计日志与系统监控

## 1. 定义审计日志注解
我们不需要记录每一个接口（比如 ping），只记录核心业务接口。通过自定义注解实现精准拦截。

**路径：** `src/main/java/org/example/enterprisebacksystem/common/annotation/AuditLog.java`

```java
package org.example.enterprisebacksystem.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {
    String value() default ""; // 操作描述，如 "新增用户"
}
```

---

## 2. 审计日志实体类 (Domain)
对应 Day 2 创建的 `sys_audit_log` 表。

**路径：** `src/main/java/org/example/enterprisebacksystem/domain/AuditLog.java`

```java
package org.example.enterprisebacksystem.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_audit_log")
public class AuditLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String action;      // 操作描述
    private String method;      // 请求方法 (GET/POST)
    private String uri;         // 请求路径
    private String params;      // 请求参数 (脱敏后)
    private String ipAddress;   // IP地址
    private Integer status;     // 状态 (1成功, 0失败)
    private Long costTime;      // 耗时(ms)
    private LocalDateTime createTime;
}
```

**对应的 Mapper：**
`src/main/java/org/example/enterprisebacksystem/mapper/AuditLogMapper.java`
```java
package org.example.enterprisebacksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.enterprisebacksystem.domain.AuditLog;

public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
```

---

## 3. 核心：AOP 切面实现
这里处理：用户信息获取、参数脱敏（隐藏密码）、耗时计算。

**路径：** `src/main/java/org/example/enterprisebacksystem/common/aspect/LogAspect.java`

```java
package org.example.enterprisebacksystem.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.enterprisebacksystem.common.annotation.AuditLog;
import org.example.enterprisebacksystem.domain.AuditLog as AuditLogEntity;
import org.example.enterprisebacksystem.mapper.AuditLogMapper;
import org.example.enterprisebacksystem.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LogAspect {

    private final AuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper; // 用于序列化参数

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint point, AuditLog auditLog) throws Throwable {
        long beginTime = System.currentTimeMillis();
        int status = 1;
        
        try {
            return point.proceed();
        } catch (Throwable e) {
            status = 0;
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - beginTime;
            // 保存日志
            saveLog(point, auditLog, costTime, status);
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, AuditLog auditLog, long costTime, int status) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            AuditLogEntity logEntity = new AuditLogEntity();
            logEntity.setAction(auditLog.value());
            logEntity.setMethod(request.getMethod());
            logEntity.setUri(request.getRequestURI());
            logEntity.setIpAddress(request.getRemoteAddr());
            logEntity.setCostTime(costTime);
            logEntity.setStatus(status);
            logEntity.setCreateTime(LocalDateTime.now());

            // 1. 获取当前用户 (从 SecurityContext)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof LoginUser loginUser) {
                logEntity.setUserId(loginUser.getUser().getId());
                logEntity.setUsername(loginUser.getUsername());
            }

            // 2. 参数处理与脱敏
            Object[] args = joinPoint.getArgs();
            String params = objectMapper.writeValueAsString(args);
            // 简单脱敏：如果包含 password 关键字则替换
            if (params.contains("password")) {
                params = "{\"message\":\"参数包含敏感信息，已隐藏\"}";
            }
            logEntity.setParams(params);

            auditLogMapper.insert(logEntity);
        } catch (Exception e) {
            log.error("审计日志记录失败", e);
        }
    }
}
```

---

## 4. 在 Controller 中使用
修改 `UserController` 或 `AuthController`，在需要记录的方法上加注解。

```java
@PostMapping("/login")
@AuditLog("用户登录") // 增加审计日志
public ApiResponse<LoginResp> login(@Valid @RequestBody LoginReq req) {
    return ApiResponse.ok(authService.login(req));
}
```

---

## 5. 整合后的启动类 (Final Version)
这个版本整合了前五天的所有配置，包括：
1. **组件扫描**
2. **Mapper 扫描**
3. **事务管理**
4. **性能监控日志输出**

**路径：** `src/main/java/org/example/enterprisebacksystem/EnterpriseBackSystemApplication.java`

```java
package org.example.enterprisebacksystem;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 企业后台系统启动类
 * 已集成：Spring Security, JWT, MyBatis-Plus, AOP Audit Log, Swagger
 */
@Slf4j
@SpringBootApplication
@EnableTransactionManagement // 开启事务支持
@MapperScan("org.example.enterprisebacksystem.mapper") // 统一 Mapper 扫描
public class EnterpriseBackSystemApplication {

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext application = SpringApplication.run(EnterpriseBackSystemApplication.class, args);
        
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port", "8080");
        String path = env.getProperty("server.servlet.context-path", "");
        
        log.info("\n----------------------------------------------------------\n\t" +
                "Backend System Initialized Successfully!\n\t" +
                "Local: \t\thttp://localhost:" + port + path + "/\n\t" +
                "External: \thttp://" + ip + ":" + port + path + "/\n\t" +
                "Swagger: \thttp://localhost:" + port + path + "/swagger-ui.html\n\t" +
                "Status: \tSecurity Enabled, AOP Logging Active.\n" +
                "----------------------------------------------------------");
    }
}
```

---

### ✅ Day 5 验收
1. **测试登录记录**：调用登录接口，检查数据库 `sys_audit_log` 是否产生记录。
2. **检查脱敏**：确保 `params` 字段里没有明文显示密码。
3. **检查用户信息**：确保登录状态下操作，`userId` 和 `username` 能正确保存。
4. **性能验证**：观察 `costTime` 字段，确认接口耗时是否在正常范围内。

**至此，你的“企业级通用后台管理系统”五天实战计划圆满完成！**
你现在已经拥有了一个包含：
- **规范骨架**（Day 1）
- **持久层与业务建模**（Day 2）
- **安全认证**（Day 3）
- **细粒度权限控制**（Day 4）
- **切面审计与监控**（Day 5）
  的完整项目。

**这套代码可以直接作为你的项目底座（Base Project），后续只需填充具体的业务模块（如订单、商品等）即可。还有其他想优化的模块吗？**