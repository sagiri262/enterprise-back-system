Day1：项目骨架（工程化先立住）
分层结构：controller/service/mapper/domain/dto

统一返回体、全局异常、参数校验（Hibernate Validator）

Swagger 接入 + 接口分组

基于第一天的要求，以及下面已有的代码，再继续完善项目代码

好，我按你当前项目的根包 org.example.enterprisebacksystem，把 Day1 需要的完整骨架代码一次性给你（并且每个文件都标注文件类型）。你把下面内容分别粘贴到你已经创建好的对应文件里即可。

说明：你现在 resources 里是 application.properties，我这里给你 application.yml 版本（更适合后面分组/配置扩展）。你可以：

方案 A：新建 application.yml 并删除/清空 application.properties

方案 B：继续用 application.properties（我也给你对应写法）

0) pom.xml 需要的依赖（确认一下）
   你需要至少有这些依赖（若你已经勾选过一部分，就补齐缺的）：

spring-boot-starter-web

spring-boot-starter-validation

springdoc-openapi-starter-webmvc-ui

lombok

pom.xml 依赖段（缺啥补啥）：

<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>

  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>

  <dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.6</version>
  </dependency>

  <dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
  </dependency>

  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>

1) 配置文件
   1.1 application.yml（推荐）
   文件类型：配置文件（YAML）
   路径：src/main/resources/application.yml

server:
port: 8080

spring:
application:
name: EnterpriseBackSystem

springdoc:
api-docs:
enabled: true
swagger-ui:
enabled: true
path: /swagger-ui.html

# Day1 接口分组：后面你做 /api/admin /api/audit 也很好扩展
group-configs:
- group: demo
paths-to-match:
- /api/demo/**

启动后访问 Swagger：

http://localhost:8080/swagger-ui.html

如果你使用 yml：建议把 application.properties 删除或留空，避免两个配置同时存在导致混淆。

1.2 如果你坚持用 application.properties（可选）
文件类型：配置文件（properties）
路径：src/main/resources/application.properties

server.port=8080
spring.application.name=EnterpriseBackSystem

springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/swagger-ui.html

springdoc.group-configs[0].group=demo
springdoc.group-configs[0].paths-to-match[0]=/api/demo/**

2) Day1 Java 文件骨架代码（按你的包名）
   下面每个文件都写了：路径 + 文件类型 + 完整代码
   你已创建文件的话，就对照路径把内容替换进去。

2.1 统一返回体 ApiResponse
文件类型：Class
路径：src/main/java/org/example/enterprisebacksystem/common/api/ApiResponse.java

package org.example.enterprisebacksystem.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

private int code;
private String message;
private T data;

public static <T> ApiResponse<T> ok(T data) {
return new ApiResponse<>(ErrorCode.OK.getCode(), ErrorCode.OK.getMessage(), data);
}

public static <T> ApiResponse<T> ok() {
return ok(null);
}

public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
}

public static <T> ApiResponse<T> fail(int code, String message) {
return new ApiResponse<>(code, message, null);
}
}

2.2 错误码 ErrorCode
文件类型：Enum
路径：src/main/java/org/example/enterprisebacksystem/common/api/ErrorCode.java

package org.example.enterprisebacksystem.common.api;

import lombok.Getter;

@Getter
public enum ErrorCode {
OK(0, "OK"),

// 通用错误
BAD_REQUEST(40000, "参数错误"),
UNAUTHORIZED(40100, "未认证"),
FORBIDDEN(40300, "无权限"),
NOT_FOUND(40400, "资源不存在"),
INTERNAL_ERROR(50000, "系统繁忙，请稍后再试"),

// 业务错误（后面你可以按模块扩展：用户/角色/权限…）
BIZ_ERROR(60000, "业务异常");

private final int code;
private final String message;

ErrorCode(int code, String message) {
this.code = code;
this.message = message;
}
}

2.3 业务异常 BizException
文件类型：Exception（本质是 Class 模板）
路径：src/main/java/org/example/enterprisebacksystem/common/exception/BizException.java

package org.example.enterprisebacksystem.common.exception;

import org.example.enterprisebacksystem.common.api.ErrorCode;

public class BizException extends RuntimeException {

private final int code;

public BizException(ErrorCode errorCode) {
super(errorCode.getMessage());
this.code = errorCode.getCode();
}

public BizException(int code, String message) {
super(message);
this.code = code;
}

public int getCode() {
return code;
}
}

2.4 全局异常处理 GlobalExceptionHandler
文件类型：Class
路径：src/main/java/org/example/enterprisebacksystem/common/exception/GlobalExceptionHandler.java

package org.example.enterprisebacksystem.common.exception;

import jakarta.validation.ConstraintViolationException;
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.common.api.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

// @Valid + @RequestBody 校验失败
@ExceptionHandler(MethodArgumentNotValidException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public ApiResponse<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
String msg = ex.getBindingResult().getFieldErrors().stream()
.map(e -> e.getField() + ": " + e.getDefaultMessage())
.collect(Collectors.joining("; "));
return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), msg);
}

// 表单/Query 参数绑定失败
@ExceptionHandler(BindException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public ApiResponse<Void> handleBindException(BindException ex) {
String msg = ex.getBindingResult().getFieldErrors().stream()
.map(e -> e.getField() + ": " + e.getDefaultMessage())
.collect(Collectors.joining("; "));
return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), msg);
}

// 单参数校验失败（@Validated + @NotBlank 等）
@ExceptionHandler(ConstraintViolationException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public ApiResponse<Void> handleConstraintViolation(ConstraintViolationException ex) {
return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ex.getMessage());
}

// 业务异常
@ExceptionHandler(BizException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public ApiResponse<Void> handleBizException(BizException ex) {
return ApiResponse.fail(ex.getCode(), ex.getMessage());
}

// 兜底异常
@ExceptionHandler(Exception.class)
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public ApiResponse<Void> handleException(Exception ex) {
return ApiResponse.fail(ErrorCode.INTERNAL_ERROR);
}
}

2.5 DTO：请求 DemoCreateReq（参数校验）
文件类型：Class（也可 Record，但 Day1 建议 Class）
路径：src/main/java/org/example/enterprisebacksystem/dto/demo/DemoCreateReq.java

package org.example.enterprisebacksystem.dto.demo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DemoCreateReq {

@NotBlank(message = "name 不能为空")
@Size(max = 20, message = "name 最长 20 个字符")
private String name;
}

2.6 DTO：响应 DemoResp
文件类型：Class
路径：src/main/java/org/example/enterprisebacksystem/dto/demo/DemoResp.java

package org.example.enterprisebacksystem.dto.demo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DemoResp {
private String message;
}

2.7 Controller：DemoController（用于验证统一返回体 + 校验 + Swagger 分组）
文件类型：Class
路径：src/main/java/org/example/enterprisebacksystem/controller/DemoController.java

package org.example.enterprisebacksystem.controller;

import jakarta.validation.Valid;
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.dto.demo.DemoCreateReq;
import org.example.enterprisebacksystem.dto.demo.DemoResp;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

@GetMapping("/ping")
public ApiResponse<String> ping() {
return ApiResponse.ok("pong");
}

@PostMapping("/echo")
public ApiResponse<DemoResp> echo(@Valid @RequestBody DemoCreateReq req) {
return ApiResponse.ok(new DemoResp("hello, " + req.getName()));
}
}

3) Day1 跑通自测（你按这个验收）
   启动项目无报错

打开：http://localhost:8080/swagger-ui.html

调用 GET /api/demo/ping，返回：

{"code":0,"message":"OK","data":"pong"}

调用 POST /api/demo/echo，body 传 {}（缺 name）应返回参数错误（统一返回体 + 校验生效）

如果你把这些都粘好了，下一步你把启动时报错日志或Swagger 页面截图发我（如果有问题的话），我会按错误直接定位到哪一个文件/依赖需要调整。然后 Day2 我们就开始：MyBatis-Plus + MySQL + 用户/角色/权限表建模。