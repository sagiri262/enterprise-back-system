# 个人网站博客接口清单

本文档记录个人网站主页、文章上传、搜索、评论、注册登录与博客管理员界面使用的后端接口。

## 统一约定

| 项目 | 说明 |
| --- | --- |
| 基础路径 | `http://localhost:8080` |
| 统一返回 | `{ "code": 0, "message": "success", "data": ... }` |
| 鉴权方式 | 需要登录的接口使用请求头 `Authorization: Bearer <token>` |
| 管理员 Token | 通过 `POST /api/auth/login` 获取 |
| 普通用户 Token | 通过 `POST /api/blog/auth/login` 或 `POST /api/blog/auth/register` 获取 |
| Turnstile | 生产环境设置 `blog.turnstile.enabled=true`、`blog.turnstile.secret=<Cloudflare secret>`，前端设置 `VITE_TURNSTILE_SITE_KEY=<site key>` |
| 评论记录文件 | 默认写入 `logs/blog-comment-records.jsonl`，可用 `blog.comment-record.path` 修改 |

## 接口表

| 模块 | 方法 | 路径 | 权限 | 请求参数/Body | 返回数据 | 用途 |
| --- | --- | --- | --- | --- | --- | --- |
| 文章 | `GET` | `/api/blog/articles` | 公开 | Query: `page`, `size`, `keyword` | `Page<BlogArticle>` | 主页文章列表；`keyword` 按标题关键词匹配搜索，只返回已发布文章 |
| 文章 | `GET` | `/api/blog/articles/{id}` | 公开 | Path: `id` | `BlogArticle` | 打开文章详情，并递增阅读数 |
| 文章管理 | `GET` | `/api/blog/articles/admin` | `blog:article:list` 或 `ROLE_admin` | Query: `page`, `size`, `keyword` | `Page<BlogArticle>` | 管理员查看全部文章，包括草稿 |
| 文章管理 | `POST` | `/api/blog/articles` | `blog:article:create` 或 `ROLE_admin` | JSON: `title`, `author`, `summary`, `contentMarkdown`, `tag`, `category`, `status` | `Boolean` | 管理员上传 Markdown 文章 |
| 文章管理 | `PUT` | `/api/blog/articles/{id}` | `blog:article:update` 或 `ROLE_admin` | Path: `id`；JSON 同上传文章 | `Boolean` | 管理员修改文章 |
| 文章管理 | `DELETE` | `/api/blog/articles/{id}` | `blog:article:delete` 或 `ROLE_admin` | Path: `id` | `Boolean` | 管理员删除文章，同时删除文章下评论 |
| 评论 | `GET` | `/api/blog/comments/captcha` | 公开 | 无 | `{ id, question, image }` | 获取评论/注册用图片验证码；`image` 是 `data:image/png;base64,...`，`question` 只返回输入提示，不暴露答案；提交时传 `captchaId` 和用户输入的 `captchaAnswer` |
| 评论 | `GET` | `/api/blog/comments` | 公开 | Query: `articleId` | `List<BlogComment>` | 获取某篇文章下可见评论 |
| 评论 | `POST` | `/api/blog/comments` | 公开；可携带普通用户 Token | JSON: `articleId`, `nickname`, `email`, `content`, `turnstileToken`, `captchaId`, `captchaAnswer` | `Boolean` | 发表评论；游客和登录用户都必须通过验证码和 Turnstile |
| 评论管理 | `DELETE` | `/api/blog/comments/{id}` | `blog:comment:delete` 或 `ROLE_admin` | Path: `id` | `Boolean` | 管理员删除评论，并扣减文章评论数 |
| 博客用户 | `POST` | `/api/blog/auth/login` | 公开 | JSON: `username`, `password` | `{ token, userId, username }` | 普通用户登录后发表评论 |
| 博客用户 | `POST` | `/api/blog/auth/register` | 公开 | JSON: `username`, `password`, `nickname`, `email`, `turnstileToken`, `captchaId`, `captchaAnswer` | `{ token, userId, username }` | 普通用户注册，自动分配 `user` 角色并登录 |
| 博客用户 | `GET` | `/api/blog/auth/site-profile` | 公开 | 无 | `BlogProfileResp` | 获取个人站站主资料，主页左侧菜单使用；`zhaoyoung` 是真实用户，不再是前端硬编码账号 |
| 博客用户 | `GET` | `/api/blog/auth/me` | 普通用户/作者/管理员登录 | Header: `Authorization` | `BlogProfileResp` | 获取当前登录用户资料、角色和权限 |
| 博客用户 | `PUT` | `/api/blog/auth/me` | 普通用户/作者/管理员登录 | JSON: `nickname`, `email`, `avatar`, `bio` | `BlogProfileResp` | 修改自己的头像、昵称、邮箱和简介；头像当前支持图片 data URL、站内路径或 http(s) 地址 |
| 系统认证 | `POST` | `/api/auth/login` | 公开 | JSON: `username`, `password` | `{ token, userId, username }` | 管理员登录博客管理界面或旧后台 |
| 用户管理 | `GET` | `/api/users` | `user:list` | Query: `page`, `size`, `keyword`, `status` | `Page<User>` | 博客管理界面查看注册用户 |
| 用户管理 | `PUT` | `/api/users/{id}` | `user:update` | Path: `id`；JSON: `username`, `password?`, `nickname`, `email`, `avatar`, `bio`, `status` | `Boolean` | 博客管理界面启用/禁用用户，也可维护基本资料 |
| 用户管理 | `DELETE` | `/api/users/{id}` | `user:delete` | Path: `id` | `Boolean` | 管理员删除用户 |

## 用户与权限边界

| 账号类型 | 角色编码 | 权限范围 |
| --- | --- | --- |
| 独立管理员 | `admin` | 管理用户、角色、权限、文章和评论 |
| 站主/博客作者 | `blogger` | 查看管理文章列表、发布文章、修改文章、删除文章、发表评论 |
| 普通注册用户 | `user` | 查看文章、搜索文章、发表评论、修改自己的头像/昵称/简介 |

当前初始化脚本会创建真实站主账号 `zhaoyoung`，并将它标记为 `site_owner=1`。主页资料接口会优先读取 `site_owner=1` 的用户，因此原来写死在前端的 `Zhao Young` 已经转为数据库用户。

## Markdown 编辑器轮子

| 项目 | 说明 |
| --- | --- |
| 选型 | `md-editor-v3` |
| GitHub | `https://github.com/imzbf/md-editor-v3` |
| 安装 | `npm install md-editor-v3` |
| 编辑器用法 | `import { MdEditor } from 'md-editor-v3'`，再 `import 'md-editor-v3/lib/style.css'`，模板中使用 `<MdEditor v-model="articleForm.contentMarkdown" />` |
| 预览用法 | `import { MdPreview } from 'md-editor-v3'`，模板中使用 `<MdPreview :model-value="article.contentMarkdown" />` |

## Cloudflare Turnstile

| 项目 | 说明 |
| --- | --- |
| 官方文档 | `https://developers.cloudflare.com/turnstile/` |
| 服务端验证地址 | `https://challenges.cloudflare.com/turnstile/v0/siteverify` |
| 前端配置 | 在 `frontend/my-admin-demo/.env.development` 或生产环境变量中配置 `VITE_TURNSTILE_SITE_KEY` |
| 后端配置 | 在 `application.properties` 中配置 `blog.turnstile.enabled=true` 和 `blog.turnstile.secret` |
| 本地开发 | 当前默认 `blog.turnstile.enabled=false`，方便本地调试；上线必须改为 `true` |
