USE enterprise_db;

DROP TABLE IF EXISTS biz_expense;
DROP TABLE IF EXISTS biz_oa_process;
DROP TABLE IF EXISTS biz_notice;
DROP TABLE IF EXISTS biz_employee;
DROP TABLE IF EXISTS biz_department;
DROP TABLE IF EXISTS blog_comment;
DROP TABLE IF EXISTS blog_article;

CREATE TABLE biz_department (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父部门ID，0表示根节点',
  name VARCHAR(80) NOT NULL COMMENT '部门名称',
  code VARCHAR(50) NOT NULL COMMENT '部门编码',
  manager_employee_id BIGINT DEFAULT NULL COMMENT '部门负责人员工ID',
  description VARCHAR(200) DEFAULT NULL COMMENT '部门描述',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_biz_department_code (code),
  KEY idx_biz_department_parent_id (parent_id),
  KEY idx_biz_department_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务部门表';

CREATE TABLE biz_employee (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT DEFAULT NULL COMMENT '关联系统用户ID',
  department_id BIGINT NOT NULL COMMENT '所属部门ID',
  employee_no VARCHAR(50) NOT NULL COMMENT '员工编号',
  name VARCHAR(50) NOT NULL COMMENT '员工姓名',
  position VARCHAR(80) DEFAULT NULL COMMENT '职位',
  level VARCHAR(30) DEFAULT NULL COMMENT '组织层级：management/manager/employee',
  phone VARCHAR(30) DEFAULT NULL COMMENT '手机号',
  email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  contact_level TINYINT NOT NULL DEFAULT 1 COMMENT '联系权限等级 1员工 2负责人 3管理层',
  online_status TINYINT NOT NULL DEFAULT 0 COMMENT '在线状态 1在线 0离线',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1在职 0离职/停用',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_biz_employee_no (employee_no),
  KEY idx_biz_employee_department_id (department_id),
  KEY idx_biz_employee_user_id (user_id),
  KEY idx_biz_employee_status (status),
  CONSTRAINT fk_biz_employee_department FOREIGN KEY (department_id) REFERENCES biz_department(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工信息表';

CREATE TABLE biz_notice (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  title VARCHAR(120) NOT NULL COMMENT '消息标题',
  content VARCHAR(2000) NOT NULL COMMENT '消息内容',
  level VARCHAR(30) DEFAULT NULL COMMENT '消息级别：安全/费用/OA/系统',
  category VARCHAR(50) DEFAULT NULL COMMENT '消息分类',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1发布 0草稿',
  publish_time DATETIME DEFAULT NULL COMMENT '发布时间',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_biz_notice_category (category),
  KEY idx_biz_notice_status_publish_time (status, publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

CREATE TABLE biz_oa_process (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  title VARCHAR(100) NOT NULL COMMENT '流程名称',
  code VARCHAR(50) NOT NULL COMMENT '流程编码',
  category VARCHAR(50) DEFAULT NULL COMMENT '流程分类',
  owner_employee_id BIGINT DEFAULT NULL COMMENT '负责人员工ID',
  status_text VARCHAR(50) DEFAULT NULL COMMENT '展示状态文案',
  tone VARCHAR(30) DEFAULT NULL COMMENT '前端色彩标识',
  pending_count INT NOT NULL DEFAULT 0 COMMENT '待办数量',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_biz_oa_process_code (code),
  KEY idx_biz_oa_process_category (category),
  KEY idx_biz_oa_process_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OA流程应用表';

CREATE TABLE biz_expense (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  title VARCHAR(120) NOT NULL COMMENT '费用标题',
  category VARCHAR(50) NOT NULL COMMENT '费用分类',
  amount DECIMAL(12,2) NOT NULL COMMENT '金额',
  applicant_employee_id BIGINT DEFAULT NULL COMMENT '申请人员工ID',
  department_id BIGINT DEFAULT NULL COMMENT '所属部门ID',
  status VARCHAR(30) DEFAULT 'SUBMITTED' COMMENT '状态 SUBMITTED/APPROVED/PAID/REJECTED',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_biz_expense_category (category),
  KEY idx_biz_expense_status (status),
  KEY idx_biz_expense_department_id (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用记录表';

CREATE TABLE blog_article (
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

CREATE TABLE blog_comment (
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
  KEY idx_blog_comment_article_status (article_id, status),
  CONSTRAINT fk_blog_comment_article FOREIGN KEY (article_id) REFERENCES blog_article(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客评论表';

INSERT INTO biz_department (id, parent_id, name, code, manager_employee_id, description, status, create_time, update_time)
VALUES
  (1, 0, '管理层', 'management', 1, '公司决策和最终审批', 1, NOW(), NOW()),
  (2, 0, '人力部门', 'hr', 2, '入职、离职、升迁、调岗', 1, NOW(), NOW()),
  (3, 0, '财务部门', 'finance', 3, '报销、薪资、采购支出', 1, NOW(), NOW()),
  (4, 0, '工作部门', 'operations', 4, '项目执行和日常排班', 1, NOW(), NOW());

INSERT INTO biz_employee (id, user_id, department_id, employee_no, name, position, level, phone, email, contact_level, online_status, status, create_time, update_time)
VALUES
  (1, 1, 1, 'E0001', '老板', '总负责人', 'management', '13800000001', 'boss@example.com', 3, 1, 1, NOW(), NOW()),
  (2, NULL, 2, 'E0002', '人力负责人', 'HR Manager', 'manager', '13800000002', 'hr@example.com', 2, 1, 1, NOW(), NOW()),
  (3, NULL, 3, 'E0003', '会计负责人', 'Finance Manager', 'manager', '13800000003', 'finance@example.com', 2, 1, 1, NOW(), NOW()),
  (4, NULL, 4, 'E0004', '工作部门负责人', 'Operation Manager', 'manager', '13800000004', 'ops@example.com', 2, 1, 1, NOW(), NOW()),
  (5, NULL, 4, 'E0005', '业务员工', 'Project Staff', 'employee', '13800000005', 'staff@example.com', 1, 1, 1, NOW(), NOW()),
  (6, NULL, 4, 'E0006', '实习员工', 'Intern', 'employee', '13800000006', 'intern@example.com', 1, 0, 1, NOW(), NOW());

UPDATE biz_department SET manager_employee_id = 1 WHERE id = 1;
UPDATE biz_department SET manager_employee_id = 2 WHERE id = 2;
UPDATE biz_department SET manager_employee_id = 3 WHERE id = 3;
UPDATE biz_department SET manager_employee_id = 4 WHERE id = 4;

INSERT INTO biz_notice (title, content, level, category, status, publish_time, create_time, update_time)
VALUES
  ('权限变更审批', '部门负责人可以为基层员工申请临时访问权限。', '安全', 'permission', 1, NOW(), NOW(), NOW()),
  ('费用报销提醒', '本周五前提交差旅票据，逾期进入下月周期。', '费用', 'expense', 1, NOW(), NOW(), NOW()),
  ('OA流程同步', '入职登记、离职登记、调岗流程已纳入工作台。', 'OA', 'oa', 1, NOW(), NOW(), NOW());

INSERT INTO biz_oa_process (title, code, category, owner_employee_id, status_text, tone, pending_count, status, create_time, update_time)
VALUES
  ('项目审批', 'project_approval', '工作部门负责人', 4, '8 个待处理', 'blue', 8, 1, NOW(), NOW()),
  ('人员出差登记', 'travel_register', '人力负责人', 2, '2 个进行中', 'green', 2, 1, NOW(), NOW()),
  ('新入职登记', 'onboarding', '人力负责人', 2, '3 个草稿', 'amber', 3, 1, NOW(), NOW()),
  ('部门人事调动', 'transfer', '部门经理', 1, '1 个待确认', 'red', 1, 1, NOW(), NOW()),
  ('离职登记', 'offboarding', '人力负责人', 2, '0 个逾期', 'green', 0, 1, NOW(), NOW()),
  ('升迁登记', 'promotion', '管理层', 1, '4 个待评审', 'blue', 4, 1, NOW(), NOW());

INSERT INTO biz_expense (title, category, amount, applicant_employee_id, department_id, status, remark, create_time, update_time)
VALUES
  ('上海客户差旅报销', '差旅报销', 5680.00, 5, 4, 'APPROVED', '高铁和住宿', NOW(), NOW()),
  ('广州供应商拜访', '差旅报销', 7000.00, 4, 4, 'SUBMITTED', '待财务复核', NOW(), NOW()),
  ('办公用品采购', '办公采购', 4320.00, 3, 3, 'PAID', '打印耗材和文具', NOW(), NOW());

INSERT INTO blog_article (title, author, summary, content_markdown, tag, category, status, read_count, comment_count, publish_time, create_time, update_time)
VALUES
  ('从零搭一个个人博客首页', 'Zhao Young', '记录个人站首页的信息架构、组件拆分、响应式布局和后续接入文章系统的思路。', '# 从零搭一个个人博客首页\n\n这里是 Markdown 正文示例，可以继续扩展为真正的文章内容。', '前端', 'Vue', 1, 1897, 0, NOW(), NOW(), NOW()),
  ('企业后台系统的权限模型整理', 'Zhao Young', '梳理用户、角色、权限、审计日志之间的关系，让后端接口和前端页面更容易继续扩展。', '# 企业后台系统的权限模型整理\n\n用户、角色和权限需要保持清晰边界，后台接口再用权限注解保护。', '后端', 'Spring Boot', 1, 936, 0, NOW(), NOW(), NOW()),
  ('把站内搜索做成可替换模块', 'Zhao Young', '先实现标题关键词匹配搜索，后续可以替换为 MiniSearch、Pagefind、Algolia 或自己的搜索接口。', '# 把站内搜索做成可替换模块\n\n当前搜索接口按标题关键词匹配，后续再接全文搜索。', '搜索', '工程化', 1, 704, 0, NOW(), NOW(), NOW());

INSERT INTO sys_permission (parent_id, name, code, type, path, create_time, update_time)
VALUES
  (0, '部门管理', 'department:list', 1, '/departments', NOW(), NOW()),
  (0, '新增部门', 'department:create', 2, NULL, NOW(), NOW()),
  (0, '修改部门', 'department:update', 2, NULL, NOW(), NOW()),
  (0, '删除部门', 'department:delete', 2, NULL, NOW(), NOW()),
  (0, '员工管理', 'employee:list', 1, '/employees', NOW(), NOW()),
  (0, '新增员工', 'employee:create', 2, NULL, NOW(), NOW()),
  (0, '修改员工', 'employee:update', 2, NULL, NOW(), NOW()),
  (0, '删除员工', 'employee:delete', 2, NULL, NOW(), NOW()),
  (0, '消息管理', 'notice:list', 1, '/notices', NOW(), NOW()),
  (0, '新增消息', 'notice:create', 2, NULL, NOW(), NOW()),
  (0, '修改消息', 'notice:update', 2, NULL, NOW(), NOW()),
  (0, '删除消息', 'notice:delete', 2, NULL, NOW(), NOW()),
  (0, 'OA流程管理', 'oa:list', 1, '/oa-processes', NOW(), NOW()),
  (0, '新增OA流程', 'oa:create', 2, NULL, NOW(), NOW()),
  (0, '修改OA流程', 'oa:update', 2, NULL, NOW(), NOW()),
  (0, '删除OA流程', 'oa:delete', 2, NULL, NOW(), NOW()),
  (0, '费用管理', 'expense:list', 1, '/expenses', NOW(), NOW()),
  (0, '新增费用', 'expense:create', 2, NULL, NOW(), NOW()),
  (0, '修改费用', 'expense:update', 2, NULL, NOW(), NOW()),
  (0, '删除费用', 'expense:delete', 2, NULL, NOW(), NOW());

INSERT INTO sys_permission (parent_id, name, code, type, path, create_time, update_time)
VALUES
  (0, '博客文章管理', 'blog:article:list', 1, '/blog/articles', NOW(), NOW()),
  (0, '新增博客文章', 'blog:article:create', 2, NULL, NOW(), NOW()),
  (0, '修改博客文章', 'blog:article:update', 2, NULL, NOW(), NOW()),
  (0, '删除博客文章', 'blog:article:delete', 2, NULL, NOW(), NOW()),
  (0, '博客评论', 'blog:comment:create', 2, NULL, NOW(), NOW()),
  (0, '删除博客评论', 'blog:comment:delete', 2, NULL, NOW(), NOW());

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, p.id
FROM sys_permission p
WHERE p.code IN (
  'department:list', 'department:create', 'department:update', 'department:delete',
  'employee:list', 'employee:create', 'employee:update', 'employee:delete',
  'notice:list', 'notice:create', 'notice:update', 'notice:delete',
  'oa:list', 'oa:create', 'oa:update', 'oa:delete',
  'expense:list', 'expense:create', 'expense:update', 'expense:delete'
  , 'blog:article:list', 'blog:article:create', 'blog:article:update', 'blog:article:delete', 'blog:comment:create', 'blog:comment:delete'
)
AND NOT EXISTS (
  SELECT 1 FROM sys_role_permission rp WHERE rp.role_id = 1 AND rp.permission_id = p.id
);

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 2, p.id
FROM sys_permission p
WHERE p.code IN ('blog:comment:create')
AND NOT EXISTS (
  SELECT 1 FROM sys_role_permission rp WHERE rp.role_id = 2 AND rp.permission_id = p.id
);
