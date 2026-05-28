CREATE DATABASE IF NOT EXISTS enterprise_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE enterprise_db;

DROP TABLE IF EXISTS sys_audit_log;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码',
  nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  avatar LONGTEXT DEFAULT NULL COMMENT '头像，开发期可存data URL，生产建议存OSS地址',
  bio VARCHAR(300) DEFAULT NULL COMMENT '个人简介',
  site_owner TINYINT DEFAULT 0 COMMENT '是否个人站站主 1是 0否',
  status TINYINT DEFAULT 1 COMMENT '状态 1启用 0禁用',
  deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  create_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE sys_role (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL COMMENT '角色名',
  code VARCHAR(50) NOT NULL COMMENT '角色编码',
  description VARCHAR(200) DEFAULT NULL COMMENT '描述',
  create_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE sys_permission (
  id BIGINT NOT NULL AUTO_INCREMENT,
  parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
  name VARCHAR(50) NOT NULL COMMENT '权限名称',
  code VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
  type TINYINT DEFAULT 1 COMMENT '类型 1菜单 2按钮',
  path VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
  create_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

CREATE TABLE sys_user_role (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE sys_role_permission (
  id BIGINT NOT NULL AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

CREATE TABLE sys_audit_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT DEFAULT NULL,
  user_name VARCHAR(50) DEFAULT NULL,
  action VARCHAR(100) DEFAULT NULL COMMENT '操作描述',
  method VARCHAR(20) DEFAULT NULL COMMENT '请求方法',
  url VARCHAR(500) DEFAULT NULL COMMENT '请求地址',
  params TEXT DEFAULT NULL COMMENT '请求参数',
  ip_address VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  status TINYINT DEFAULT NULL COMMENT '状态 1成功 0失败',
  cost_time BIGINT DEFAULT NULL COMMENT '耗时毫秒',
  create_time DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_create_time (create_time),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

INSERT INTO sys_user (id, username, password, nickname, email, avatar, bio, site_owner, status, deleted, create_time, update_time)
VALUES
  (1, 'admin', '$2a$10$g8/wbR.jVZH8He.3lTVur.C70LNIuGUbtkgjHnh5bs8O1wLgLBmoa', '系统管理员', 'admin@example.com', NULL, '独立管理员账号，只用于系统管理。', 0, 1, 0, NOW(), NOW()),
  (2, 'zhaoyoung', '$2a$10$g8/wbR.jVZH8He.3lTVur.C70LNIuGUbtkgjHnh5bs8O1wLgLBmoa', 'Zhao Young', 'zhaoyoung@example.com', NULL, '把工程、写作和一点审美放在同一个工作台上。', 1, 1, 0, NOW(), NOW());

INSERT INTO sys_role (id, name, code, description, create_time, update_time)
VALUES
  (1, '超级管理员', 'admin', '拥有系统全部权限', NOW(), NOW()),
  (2, '普通用户', 'user', '默认业务用户', NOW(), NOW()),
  (3, '博客作者', 'blogger', '站主作者账号，可发布、修改和删除博客文章', NOW(), NOW());

INSERT INTO sys_permission (id, parent_id, name, code, type, path, create_time, update_time)
VALUES
  (1, 0, '用户管理', 'user:list', 1, '/users', NOW(), NOW()),
  (2, 1, '新增用户', 'user:create', 2, NULL, NOW(), NOW()),
  (3, 1, '分配角色', 'user:assign-role', 2, NULL, NOW(), NOW()),
  (4, 1, '修改用户', 'user:update', 2, NULL, NOW(), NOW()),
  (5, 1, '删除用户', 'user:delete', 2, NULL, NOW(), NOW()),
  (6, 0, '角色管理', 'role:list', 1, '/roles', NOW(), NOW()),
  (7, 6, '新增角色', 'role:create', 2, NULL, NOW(), NOW()),
  (8, 6, '修改角色', 'role:update', 2, NULL, NOW(), NOW()),
  (9, 6, '删除角色', 'role:delete', 2, NULL, NOW(), NOW()),
  (10, 6, '分配权限', 'role:assign-permission', 2, NULL, NOW(), NOW()),
  (11, 0, '权限管理', 'permission:list', 1, '/permissions', NOW(), NOW()),
  (12, 11, '新增权限', 'permission:create', 2, NULL, NOW(), NOW()),
  (13, 11, '修改权限', 'permission:update', 2, NULL, NOW(), NOW()),
  (14, 11, '删除权限', 'permission:delete', 2, NULL, NOW(), NOW());

INSERT INTO sys_user_role (user_id, role_id)
VALUES
  (1, 1),
  (2, 3);

INSERT INTO sys_role_permission (role_id, permission_id)
VALUES
  (1, 1),
  (1, 2),
  (1, 3),
  (1, 4),
  (1, 5),
  (1, 6),
  (1, 7),
  (1, 8),
  (1, 9),
  (1, 10),
  (1, 11),
  (1, 12),
  (1, 13),
  (1, 14);
