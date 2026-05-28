# 个人网站阿里云 ECS 上线部署流程

## 1. 当前环境和生产环境的区别

当前项目运行在本地开发环境：

| 模块 | 本地开发环境 | 说明 |
| --- | --- | --- |
| 前端 | Vite dev server，默认 `http://localhost:5173` | 代码热更新，适合边改边看效果 |
| 后端 | Spring Boot，默认 `http://localhost:8080` | 使用 `./mvnw spring-boot:run` 启动 |
| 数据库 | 本机 MySQL，库名 `enterprise_db` | 用 `src/main/resources/sql/*.sql` 初始化 |
| 人机验证 | `blog.turnstile.enabled=false` | 本地可跳过 Cloudflare Turnstile |
| 访问方式 | localhost | 只能本机访问 |

正式生产环境建议是：

| 模块 | 生产环境建议 |
| --- | --- |
| 前端 | `npm run build` 后由 Nginx 托管静态文件 |
| 后端 | 打成 jar，用 systemd 常驻运行在 ECS |
| 数据库 | 阿里云 RDS MySQL，或 ECS 自建 MySQL |
| 反向代理 | Nginx 将 `/api/` 转发到 `127.0.0.1:8080` |
| 域名 | 解析到 ECS 公网 IP |
| HTTPS | 阿里云免费证书或 Let's Encrypt |
| 人机验证 | 开启 Cloudflare Turnstile，配置真实 site key 和 secret |
| 密码/JWT | 不把数据库密码、密钥写死在代码仓库，改用环境变量或服务器配置文件 |

## 2. 上线前准备

1. 购买阿里云 ECS，建议先选 2 核 2G 或以上。
2. 系统建议使用 Ubuntu 22.04/24.04 或 Alibaba Cloud Linux 3。
3. 安全组开放：
   - `22`：SSH，仅允许自己的 IP 更安全。
   - `80`：HTTP。
   - `443`：HTTPS。
   - 不建议公网开放 `3306` 和 `8080`。
4. 准备域名，并把域名 A 记录解析到 ECS 公网 IP。
5. 准备 Cloudflare Turnstile：
   - 前端 site key：构建时写入 `VITE_TURNSTILE_SITE_KEY`。
   - 后端 secret：写入生产配置 `blog.turnstile.secret`。

## 3. 服务器安装基础软件

Ubuntu 示例：

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk nginx mysql-server git
java -version
nginx -v
mysql --version
```

Node.js 建议安装 22 LTS：

```bash
curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -
sudo apt install -y nodejs
node -v
npm -v
```

如果使用阿里云 RDS，可以不在 ECS 安装 MySQL，只需要确保 ECS 能访问 RDS 内网地址。

## 4. 数据库初始化

登录 MySQL：

```bash
sudo mysql
```

创建生产库和账号，示例：

```sql
CREATE DATABASE IF NOT EXISTS enterprise_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'enterprise_app'@'localhost' IDENTIFIED BY '请替换为强密码';
GRANT ALL PRIVILEGES ON enterprise_db.* TO 'enterprise_app'@'localhost';
FLUSH PRIVILEGES;
```

初始化表结构和博客数据：

```bash
mysql -u enterprise_app -p enterprise_db < src/main/resources/sql/init.sql
mysql -u enterprise_app -p enterprise_db < src/main/resources/sql/blog_init.sql
mysql -u enterprise_app -p enterprise_db < src/main/resources/sql/business_init.sql
```

已有数据库升级时，不要重复执行会 `DROP TABLE` 的 `init.sql`。只执行幂等升级脚本，例如当前的 `blog_init.sql`。

## 5. 构建后端

在服务器项目根目录：

```bash
./mvnw clean package -DskipTests
```

生产配置建议放到服务器，例如 `/opt/enterprise/config/application-prod.properties`：

```properties
server.port=8080
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/enterprise_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
spring.datasource.username=enterprise_app
spring.datasource.password=请替换为强密码

mybatis-plus.configuration.log-impl=

blog.turnstile.enabled=true
blog.turnstile.secret=请替换为CloudflareTurnstileSecret
blog.comment-record.path=/var/log/enterprise/blog-comment-records.jsonl
```

创建日志目录：

```bash
sudo mkdir -p /var/log/enterprise
sudo chown -R $USER:$USER /var/log/enterprise
```

创建 systemd 服务 `/etc/systemd/system/enterprise-backend.service`：

```ini
[Unit]
Description=Enterprise Personal Site Backend
After=network.target

[Service]
Type=simple
WorkingDirectory=/opt/enterprise/EnterpriseBackSystem
ExecStart=/usr/bin/java -jar /opt/enterprise/EnterpriseBackSystem/target/EnterpriseBackSystem-0.0.1-SNAPSHOT.jar --spring.config.additional-location=file:/opt/enterprise/config/application-prod.properties
Restart=always
RestartSec=5
User=www-data

[Install]
WantedBy=multi-user.target
```

启动后端：

```bash
sudo systemctl daemon-reload
sudo systemctl enable enterprise-backend
sudo systemctl start enterprise-backend
sudo systemctl status enterprise-backend
```

检查接口：

```bash
curl http://127.0.0.1:8080/api/blog/auth/site-profile
```

## 6. 构建前端

进入前端目录：

```bash
cd frontend/my-admin-demo
```

创建生产环境变量 `.env.production`：

```properties
VITE_API_BASE_URL=
VITE_TURNSTILE_SITE_KEY=请替换为CloudflareTurnstileSiteKey
```

构建：

```bash
npm ci
npm run build
```

把 `dist` 放到 Nginx 目录：

```bash
sudo mkdir -p /var/www/personal-site
sudo rsync -av --delete dist/ /var/www/personal-site/
```

## 7. 配置 Nginx

创建 `/etc/nginx/sites-available/personal-site.conf`：

```nginx
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;

    root /var/www/personal-site;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

启用配置：

```bash
sudo ln -s /etc/nginx/sites-available/personal-site.conf /etc/nginx/sites-enabled/personal-site.conf
sudo nginx -t
sudo systemctl reload nginx
```

## 8. 配置 HTTPS

可以使用阿里云证书服务下载 Nginx 证书，或使用 Let's Encrypt：

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com -d www.your-domain.com
```

完成后确认：

```bash
curl -I https://your-domain.com
```

## 9. 首次上线后的账号检查

默认管理员账号：

| 账号 | 用途 |
| --- | --- |
| `admin` | 独立管理员账号，可以管理用户、文章、评论 |
| `zhaoyoung` | 个人站站主/博客作者账号，可以发布、修改、删除文章 |
| 普通注册用户 | 只能查看文章、搜索文章、发表评论、修改自己的头像/昵称/简介 |

生产环境上线后必须立即修改默认密码。当前项目使用 BCrypt 存储密码，不建议使用 SHA256 直接存密码。

## 10. 更新发布流程

每次改代码后：

```bash
git pull
./mvnw test
./mvnw clean package -DskipTests
sudo systemctl restart enterprise-backend

cd frontend/my-admin-demo
npm ci
npm run build
sudo rsync -av --delete dist/ /var/www/personal-site/
sudo systemctl reload nginx
```

## 11. 生产环境注意事项

1. 数据库密码、Turnstile secret、JWT 密钥不要提交到 Git。
2. `mybatis-plus.configuration.log-impl` 生产环境应关闭，避免日志过多和敏感信息泄露。
3. 头像目前支持存 data URL，个人站初期够用；正式长期使用建议上传到阿里云 OSS，数据库只保存 OSS URL。
4. 评论记录文件建议写到 `/var/log/enterprise/blog-comment-records.jsonl` 并配置 logrotate。
5. ECS 安全组不要开放 `8080`，只让 Nginx 访问后端。
6. 数据库定期备份，至少每日备份一次。
7. 上线后用普通用户、站主作者、管理员三类账号分别测试权限边界。
