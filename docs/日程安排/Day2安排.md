Day2 **还没有完成**（按你自己写的 Day2 目标：**完成 user/role/permission 的 CRUD** 来验收）。你目前这份内容里**明确缺少** Role / Permission 的一整套 CRUD 代码（以及一些“地基配置/脚本”如果你还没落地到项目里也算未完成）。

下面我按“必须完成（Day2 验收）”列出**还需要补的代码文件**；每个文件我都标了 **文件类型**（Class / Interface / Enum / …）。

---

## Day2 还没完成的部分（必须补齐才能算完成）

### A. Role 的 CRUD（缺）

* `src/main/java/org/example/enterprisebacksystem/domain/Role.java` —— **Class**
* `src/main/java/org/example/enterprisebacksystem/mapper/RoleMapper.java` —— **Interface**
* `src/main/java/org/example/enterprisebacksystem/service/RoleService.java` —— **Interface**
* `src/main/java/org/example/enterprisebacksystem/service/impl/RoleServiceImpl.java` —— **Class**
* `src/main/java/org/example/enterprisebacksystem/dto/role/RoleSaveReq.java` —— **Class**
* `src/main/java/org/example/enterprisebacksystem/controller/RoleController.java` —— **Class**

### B. Permission 的 CRUD（缺）

* `src/main/java/org/example/enterprisebacksystem/domain/Permission.java` —— **Class**
* `src/main/java/org/example/enterprisebacksystem/mapper/PermissionMapper.java` —— **Interface**
* `src/main/java/org/example/enterprisebacksystem/service/PermissionService.java` —— **Interface**
* `src/main/java/org/example/enterprisebacksystem/service/impl/PermissionServiceImpl.java` —— **Class**
* `src/main/java/org/example/enterprisebacksystem/dto/permission/PermissionSaveReq.java` —— **Class**
* `src/main/java/org/example/enterprisebacksystem/controller/PermissionController.java` —— **Class**

---

## Day2 基建部分（如果你还没实际写进项目/跑通，也算未完成）

### C. MyBatis-Plus 配置与基类（若未创建）

* `src/main/java/org/example/enterprisebacksystem/config/MybatisPlusConfig.java` —— **Class**
* `src/main/java/org/example/enterprisebacksystem/domain/BaseEntity.java` —— **Class**

### D. 数据库脚本与配置（不是 Java 文件，但 Day2 必须落地）

* `application.yml`（或 `application.properties`）里数据源 + mybatis-plus 配置 —— **配置文件**
* 建表 SQL（`sys_user/sys_role/sys_permission/...`）在数据库执行成功 —— **SQL 脚本**

---

## 你提到的这几个文件类型，Day2 会用到哪些？

* **Class**：实体、DTO、Controller、ServiceImpl、Config
* **Interface**：Mapper、Service
* **Enum / Record / Annotation / Exception**：Day2 不强制新增（Exception 你 Day1 已有 BizException）

---

### 如果你想我“精确判断你项目里到底缺哪些文件”

你把现在的 `src/main/java/org/example/enterprisebacksystem` 目录下的 `tree` 再贴一次（或截图也行），我就能对照 Day2 清单告诉你：**哪些已完成、哪些缺、哪些多余**，并且下一步直接给你补齐 Role/Permission 的完整 CRUD 代码。


太棒了！我们进入 **Day2：数据持久层与核心业务建模**。

今天的工作量比较扎实，涉及到**数据库连接**、**MyBatis-Plus 配置**、**SQL 建表**以及**核心 CRUD 代码**的编写。为了让你能清晰地一步步完成，我将内容分为 **“环境搭建”** 和 **“业务实现”** 两大部分。

---

### 🚀 第一部分：环境与配置 (Infrastructure)

这部分是地基，必须先跑通。

#### 1. 修改 `pom.xml` (追加依赖)

在 `<dependencies>` 中追加以下 **MySQL** 和 **MyBatis-Plus** 的依赖：

```xml
<!-- Database -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- MyBatis Plus -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.7</version>
</dependency>
```

#### 2. 修改 `application.yml` (连接数据库)

请根据你本地数据库的情况修改 `url`, `username`, `password`。

```yaml
spring:
  application:
    name: EnterpriseBackSystem
  # --- 新增数据源配置 ---
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    # 请修改为你的库名、账号、密码
    url: jdbc:mysql://localhost:3306/enterprise_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: password

# --- 新增 MyBatis-Plus 配置 ---
mybatis-plus:
  configuration:
    # 开启驼峰命名映射 (user_name -> userName)
    map-underscore-to-camel-case: true
    # 开发环境打印 SQL 日志
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      # 逻辑删除默认值 (0-未删, 1-已删)
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

#### 3. 初始化 SQL 脚本

请在你的数据库（如 `enterprise_db`）中执行以下 SQL。这涵盖了你要求的 User, Role, Permission, AuditLog 及关联表。

```sql
-- 1. 用户表
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `status` tinyint DEFAULT '1' COMMENT '状态 1:启用 0:禁用',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 角色表
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '角色名',
  `code` varchar(50) NOT NULL COMMENT '角色编码 (admin, user)',
  `description` varchar(200) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. 权限表 (菜单/按钮)
CREATE TABLE `sys_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint DEFAULT '0' COMMENT '父ID',
  `name` varchar(50) NOT NULL COMMENT '权限名称',
  `code` varchar(100) DEFAULT NULL COMMENT '权限标识 (user:add)',
  `type` tinyint DEFAULT '1' COMMENT '类型 1:菜单 2:按钮',
  `path` varchar(200) DEFAULT NULL COMMENT '路由路径',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 4. 关联表
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

CREATE TABLE `sys_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `permission_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联';

-- 5. 审计日志表
CREATE TABLE `sys_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `username` varchar(50) DEFAULT NULL,
  `action` varchar(100) DEFAULT NULL COMMENT '操作描述',
  `method` varchar(200) DEFAULT NULL COMMENT '请求方法',
  `ip_address` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志';
```

---

### 💻 第二部分：Java 代码实现

请按以下顺序创建文件。

#### 1. 基础配置与基类

**路径：** `src/main/java/org/example/enterprisebacksystem/config/MybatisPlusConfig.java`
*(作用：配置自动填充时间，扫描 Mapper)*

```java
package org.example.enterprisebacksystem.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
@MapperScan("org.example.enterprisebacksystem.mapper") // 扫描 Mapper 接口
public class MybatisPlusConfig implements MetaObjectHandler {

    // 分页插件
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    // 自动填充 createTime 和 updateTime
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
```

**路径：** `src/main/java/org/example/enterprisebacksystem/domain/BaseEntity.java`
*(作用：实体父类，统一定义通用字段)*

```java
package org.example.enterprisebacksystem.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BaseEntity implements Serializable {
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

#### 2. 实体类 (Domain)

**路径：** `src/main/java/org/example/enterprisebacksystem/domain/User.java`

```java
package org.example.enterprisebacksystem.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private Integer status; // 1:启用 0:禁用
    @TableLogic
    private Integer deleted;
}
```

**路径：** `src/main/java/org/example/enterprisebacksystem/domain/Role.java`

```java
package org.example.enterprisebacksystem.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class Role extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private String description;
}
```

**路径：** `src/main/java/org/example/enterprisebacksystem/domain/Permission.java`

```java
package org.example.enterprisebacksystem.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class Permission extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String name;
    private String code;
    private Integer type; // 1:菜单 2:按钮
    private String path;
}
```

*(AuditLog 和 关联表实体省略，通常不需要直接 CRUD，用到时再加)*

#### 3. Mapper 接口

**路径：** `src/main/java/org/example/enterprisebacksystem/mapper/UserMapper.java`
*(RoleMapper, PermissionMapper 同理，只需改类名和泛型，这里以 User 为例，你需要**照猫画虎**创建另外两个)*

```java
package org.example.enterprisebacksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.enterprisebacksystem.domain.User;

public interface UserMapper extends BaseMapper<User> {
}
```

*(请务必创建 `RoleMapper.java` 和 `PermissionMapper.java`，内容与上面一致，只是泛型改为 `<Role>` 和 `<Permission>`)*

#### 4. Service 层 (利用 MP 的 IService)

**路径：** `src/main/java/org/example/enterprisebacksystem/service/UserService.java`

```java
package org.example.enterprisebacksystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.enterprisebacksystem.domain.User;

public interface UserService extends IService<User> {
}
```

**路径：** `src/main/java/org/example/enterprisebacksystem/service/impl/UserServiceImpl.java`

```java
package org.example.enterprisebacksystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.enterprisebacksystem.domain.User;
import org.example.enterprisebacksystem.mapper.UserMapper;
import org.example.enterprisebacksystem.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
```

*(同样，请自行创建 RoleService/RoleServiceImpl 和 PermissionService/PermissionServiceImpl)*

#### 5. Controller 层 (CRUD 核心)

为了演示完整性，我提供一个**标准化**的 `UserController`。Role 和 Permission 的 Controller 可以完全模仿这个结构。

**需要先补一个 UserDTO** (用于新增/修改传参):
`src/main/java/org/example/enterprisebacksystem/dto/user/UserSaveReq.java`

```java
package org.example.enterprisebacksystem.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserSaveReq {
    @NotBlank(message = "用户名不能为空")
    private String username;
    private String nickname;
    private String email;
    // 密码暂时明文，Day3 做加密
    private String password;
}
```

**路径：** `src/main/java/org/example/enterprisebacksystem/controller/UserController.java`

```java
package org.example.enterprisebacksystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.domain.User;
import org.example.enterprisebacksystem.dto.user.UserSaveReq;
import org.example.enterprisebacksystem.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 1. 分页查询
    @GetMapping
    public ApiResponse<Page<User>> page(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(required = false) String username) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        // 如果传了 username 就模糊查询
        wrapper.like(username != null && !username.isEmpty(), User::getUsername, username);
        // 按创建时间倒序
        wrapper.orderByDesc(User::getCreateTime);
        
        return ApiResponse.ok(userService.page(pageParam, wrapper));
    }

    // 2. 新增
    @PostMapping
    public ApiResponse<Boolean> create(@Valid @RequestBody UserSaveReq req) {
        User user = new User();
        BeanUtils.copyProperties(req, user);
        // 默认密码处理（Day3 会完善）
        if (user.getPassword() == null) user.setPassword("123456");
        return ApiResponse.ok(userService.save(user));
    }

    // 3. 修改
    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@PathVariable Long id, @RequestBody UserSaveReq req) {
        User user = new User();
        BeanUtils.copyProperties(req, user);
        user.setId(id);
        return ApiResponse.ok(userService.updateById(user));
    }

    // 4. 删除
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        return ApiResponse.ok(userService.removeById(id));
    }
}
```

---

### ✅ Day2 验证步骤

1.  **启动 Spring Boot**，确保没有数据库连接错误。
2.  打开 Swagger (`http://localhost:8080/swagger-ui.html`)。
3.  你应该能看到 `user-controller` 分组。
4.  **测试新增接口**：创建一个用户，查看数据库 `sys_user` 表是否多了一条数据，且 `create_time` 有值（MyBatis-Plus 自动填充生效）。
5.  **测试查询接口**：调用分页接口，查看是否返回数据。

完成后，**Day3 我们将处理“密码加密(Spring Security/BCrypt)”以及“User-Role-Permission”的关联逻辑**。