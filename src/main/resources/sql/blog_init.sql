USE enterprise_db;

CREATE TABLE IF NOT EXISTS blog_article (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  title VARCHAR(160) NOT NULL COMMENT '文章标题',
  author VARCHAR(80) NOT NULL COMMENT '作者',
  summary VARCHAR(500) DEFAULT NULL COMMENT '摘要',
  content_markdown MEDIUMTEXT NOT NULL COMMENT 'Markdown正文',
  tag VARCHAR(50) DEFAULT NULL COMMENT '标签',
  category VARCHAR(80) DEFAULT NULL COMMENT '分类',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1发布 0草稿',
  read_count INT NOT NULL DEFAULT 0 COMMENT '阅读量',
  comment_count INT NOT NULL DEFAULT 0 COMMENT '评论数',
  publish_time DATETIME DEFAULT NULL COMMENT '发布时间',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_blog_article_title (title),
  KEY idx_blog_article_status_publish_time (status, publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客文章表';

CREATE TABLE IF NOT EXISTS blog_comment (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  article_id BIGINT NOT NULL COMMENT '文章ID',
  user_id BIGINT DEFAULT NULL COMMENT '评论用户ID，游客为空',
  nickname VARCHAR(80) NOT NULL COMMENT '昵称',
  email VARCHAR(120) DEFAULT NULL COMMENT '邮箱',
  content VARCHAR(1200) NOT NULL COMMENT '评论内容',
  ip_address VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1显示 0隐藏',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_blog_comment_article_status (article_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客评论表';

SET @blog_comment_user_id_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'blog_comment'
    AND column_name = 'user_id'
);
SET @blog_comment_user_id_sql := IF(
  @blog_comment_user_id_exists = 0,
  'ALTER TABLE blog_comment ADD COLUMN user_id BIGINT DEFAULT NULL COMMENT ''评论用户ID，游客为空'' AFTER article_id',
  'SELECT 1'
);
PREPARE blog_comment_user_id_stmt FROM @blog_comment_user_id_sql;
EXECUTE blog_comment_user_id_stmt;
DEALLOCATE PREPARE blog_comment_user_id_stmt;

SET @sys_user_avatar_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'sys_user'
    AND column_name = 'avatar'
);
SET @sys_user_avatar_sql := IF(
  @sys_user_avatar_exists = 0,
  'ALTER TABLE sys_user ADD COLUMN avatar LONGTEXT DEFAULT NULL COMMENT ''头像，开发期可存data URL，生产建议存OSS地址'' AFTER email',
  'SELECT 1'
);
PREPARE sys_user_avatar_stmt FROM @sys_user_avatar_sql;
EXECUTE sys_user_avatar_stmt;
DEALLOCATE PREPARE sys_user_avatar_stmt;

SET @sys_user_bio_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'sys_user'
    AND column_name = 'bio'
);
SET @sys_user_bio_sql := IF(
  @sys_user_bio_exists = 0,
  'ALTER TABLE sys_user ADD COLUMN bio VARCHAR(300) DEFAULT NULL COMMENT ''个人简介'' AFTER avatar',
  'SELECT 1'
);
PREPARE sys_user_bio_stmt FROM @sys_user_bio_sql;
EXECUTE sys_user_bio_stmt;
DEALLOCATE PREPARE sys_user_bio_stmt;

SET @sys_user_site_owner_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'sys_user'
    AND column_name = 'site_owner'
);
SET @sys_user_site_owner_sql := IF(
  @sys_user_site_owner_exists = 0,
  'ALTER TABLE sys_user ADD COLUMN site_owner TINYINT DEFAULT 0 COMMENT ''是否个人站站主 1是 0否'' AFTER bio',
  'SELECT 1'
);
PREPARE sys_user_site_owner_stmt FROM @sys_user_site_owner_sql;
EXECUTE sys_user_site_owner_stmt;
DEALLOCATE PREPARE sys_user_site_owner_stmt;

INSERT INTO sys_role (name, code, description, create_time, update_time)
SELECT '博客作者', 'blogger', '站主作者账号，可发布、修改和删除博客文章', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE code = 'blogger');

INSERT INTO sys_user (username, password, nickname, email, bio, site_owner, status, deleted, create_time, update_time)
SELECT 'zhaoyoung',
       '$2a$10$g8/wbR.jVZH8He.3lTVur.C70LNIuGUbtkgjHnh5bs8O1wLgLBmoa',
       'Zhao Young',
       'zhaoyoung@example.com',
       '把工程、写作和一点审美放在同一个工作台上。',
       1,
       1,
       0,
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'zhaoyoung');

UPDATE sys_user
SET nickname = COALESCE(NULLIF(nickname, ''), 'Zhao Young'),
    bio = COALESCE(NULLIF(bio, ''), '把工程、写作和一点审美放在同一个工作台上。'),
    site_owner = 1,
    status = 1,
    update_time = NOW()
WHERE username = 'zhaoyoung';

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
JOIN sys_role r ON r.code = 'blogger'
WHERE u.username = 'zhaoyoung'
  AND NOT EXISTS (
    SELECT 1 FROM sys_user_role ur
    WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

INSERT INTO blog_article (title, author, summary, content_markdown, tag, category, status, read_count, comment_count, publish_time, create_time, update_time)
SELECT '从零搭一个个人博客首页', 'Zhao Young', '记录个人站首页的信息架构、组件拆分、响应式布局和后续接入文章系统的思路。', '# 从零搭一个个人博客首页\n\n这里是 Markdown 正文示例，可以继续扩展为真正的文章内容。', '前端', 'Vue', 1, 1897, 0, NOW(), NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM blog_article WHERE title = '从零搭一个个人博客首页');

INSERT INTO blog_article (title, author, summary, content_markdown, tag, category, status, read_count, comment_count, publish_time, create_time, update_time)
SELECT '企业后台系统的权限模型整理', 'Zhao Young', '梳理用户、角色、权限、审计日志之间的关系，让后端接口和前端页面更容易继续扩展。', '# 企业后台系统的权限模型整理\n\n用户、角色和权限需要保持清晰边界，后台接口再用权限注解保护。', '后端', 'Spring Boot', 1, 936, 0, NOW(), NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM blog_article WHERE title = '企业后台系统的权限模型整理');

INSERT INTO blog_article (title, author, summary, content_markdown, tag, category, status, read_count, comment_count, publish_time, create_time, update_time)
SELECT '把站内搜索做成可替换模块', 'Zhao Young', '先实现标题关键词匹配搜索，后续可以替换为 MiniSearch、Pagefind、Algolia 或自己的搜索接口。', '# 把站内搜索做成可替换模块\n\n当前搜索接口按标题关键词匹配，后续再接全文搜索。', '搜索', '工程化', 1, 704, 0, NOW(), NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM blog_article WHERE title = '把站内搜索做成可替换模块');

INSERT INTO sys_permission (parent_id, name, code, type, path, create_time, update_time)
SELECT 0, '博客文章管理', 'blog:article:list', 1, '/blog/articles', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'blog:article:list');

INSERT INTO sys_permission (parent_id, name, code, type, path, create_time, update_time)
SELECT 0, '新增博客文章', 'blog:article:create', 2, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'blog:article:create');

INSERT INTO sys_permission (parent_id, name, code, type, path, create_time, update_time)
SELECT 0, '修改博客文章', 'blog:article:update', 2, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'blog:article:update');

INSERT INTO sys_permission (parent_id, name, code, type, path, create_time, update_time)
SELECT 0, '删除博客文章', 'blog:article:delete', 2, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'blog:article:delete');

INSERT INTO sys_permission (parent_id, name, code, type, path, create_time, update_time)
SELECT 0, '博客评论', 'blog:comment:create', 2, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'blog:comment:create');

INSERT INTO sys_permission (parent_id, name, code, type, path, create_time, update_time)
SELECT 0, '删除博客评论', 'blog:comment:delete', 2, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'blog:comment:delete');

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.code IN (
  'blog:article:list',
  'blog:article:create',
  'blog:article:update',
  'blog:article:delete',
  'blog:comment:create',
  'blog:comment:delete'
)
WHERE r.code = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.code = 'blog:comment:create'
WHERE r.code = 'user'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.code IN (
  'blog:article:list',
  'blog:article:create',
  'blog:article:update',
  'blog:article:delete',
  'blog:comment:create'
)
WHERE r.code = 'blogger'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

SET @audit_user_name_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'sys_audit_log'
    AND column_name = 'user_name'
);
SET @audit_user_name_sql := IF(
  @audit_user_name_exists = 0,
  'ALTER TABLE sys_audit_log ADD COLUMN user_name VARCHAR(50) DEFAULT NULL AFTER user_id',
  'SELECT 1'
);
PREPARE audit_user_name_stmt FROM @audit_user_name_sql;
EXECUTE audit_user_name_stmt;
DEALLOCATE PREPARE audit_user_name_stmt;

SET @audit_username_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'sys_audit_log'
    AND column_name = 'username'
);
SET @audit_username_sql := IF(
  @audit_username_exists > 0,
  'ALTER TABLE sys_audit_log DROP COLUMN username',
  'SELECT 1'
);
PREPARE audit_username_stmt FROM @audit_username_sql;
EXECUTE audit_username_stmt;
DEALLOCATE PREPARE audit_username_stmt;

SET @audit_url_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'sys_audit_log'
    AND column_name = 'url'
);
SET @audit_url_sql := IF(
  @audit_url_exists = 0,
  'ALTER TABLE sys_audit_log ADD COLUMN url VARCHAR(500) DEFAULT NULL AFTER method',
  'SELECT 1'
);
PREPARE audit_url_stmt FROM @audit_url_sql;
EXECUTE audit_url_stmt;
DEALLOCATE PREPARE audit_url_stmt;

SET @audit_params_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'sys_audit_log'
    AND column_name = 'params'
);
SET @audit_params_sql := IF(
  @audit_params_exists = 0,
  'ALTER TABLE sys_audit_log ADD COLUMN params TEXT DEFAULT NULL AFTER url',
  'SELECT 1'
);
PREPARE audit_params_stmt FROM @audit_params_sql;
EXECUTE audit_params_stmt;
DEALLOCATE PREPARE audit_params_stmt;

SET @audit_status_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'sys_audit_log'
    AND column_name = 'status'
);
SET @audit_status_sql := IF(
  @audit_status_exists = 0,
  'ALTER TABLE sys_audit_log ADD COLUMN status TINYINT DEFAULT NULL AFTER ip_address',
  'SELECT 1'
);
PREPARE audit_status_stmt FROM @audit_status_sql;
EXECUTE audit_status_stmt;
DEALLOCATE PREPARE audit_status_stmt;

SET @audit_cost_time_exists := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'sys_audit_log'
    AND column_name = 'cost_time'
);
SET @audit_cost_time_sql := IF(
  @audit_cost_time_exists = 0,
  'ALTER TABLE sys_audit_log ADD COLUMN cost_time BIGINT DEFAULT NULL AFTER status',
  'SELECT 1'
);
PREPARE audit_cost_time_stmt FROM @audit_cost_time_sql;
EXECUTE audit_cost_time_stmt;
DEALLOCATE PREPARE audit_cost_time_stmt;
