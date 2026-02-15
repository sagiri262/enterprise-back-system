# 企业后端管理系统

7 天（每天 6 小时）可交付排期
## Day1：项目骨架（工程化先立住）

分层结构：controller/service/mapper/domain/dto ，统一返回体、全局异常、参数校验（Hibernate Validator），Swagger 接入 + 接口分组

## Day2：数据库建模 + 基础 CRUD

表：user、role、permission、user_role、role_permission、audit_log

完成用户/角色/权限的 CRUD

## Day3：登录认证（从简单到规范）

先做：用户名+密码登录（BCrypt 加密）

登录成功返回 token（先用 JWT 或简单 session 都行；建议 JWT，更好写简历）

## Day4：鉴权（项目含金量关键点）

用 Spring Security 做：

登录过滤器

权限校验（接口需要某权限）

做到：不同角色访问同接口，结果不同

## Day5：审计日志（AOP 一把加分）

AOP 拦截 Controller

记录：userId、uri、method、参数（脱敏）、耗时、状态码

写入 audit_log 表

## Day6：补齐细节 + 规范化

分页查询、批量授权

数据字典/枚举规范

关键接口写 3~5 个测试

## Day7：交付“简历友好型仓库”

README：架构图、ER 图、鉴权流程图、快速启动

Postman/Apifox 接口集合导出

SQL 初始化脚本
