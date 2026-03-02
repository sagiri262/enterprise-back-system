今天的内容分两步走，这一步我们先打通登录接口，下一步我们写拦截器（让没登录的人无法访问接口）。

1、刷新 Maven 依赖，启动项目。
2、打开 Swagger 或 ApiPost/Postman。
3、调用 /api/auth/register-test：传入 JSON {"username":"admin2", "password":"123"}，观察数据库，此时存进去的密码应该是一长串乱码（如 $2a$10$xxxxxx...），这就是 BCrypt 加密后的结果。 
4、调用 /api/auth/login：传入刚才的账号密码。
5、验证：如果一切顺利，你将收到包含长串字母数字的 token 的 JSON 响应！