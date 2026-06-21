SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE permission_system;

-- =============================================
-- 评价系统 - 评价提交模块
-- =============================================

-- =============================================
-- 1. 评价邀请表
-- =============================================
DROP TABLE IF EXISTS review_invitation;
CREATE TABLE review_invitation (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  check_in_id BIGINT NOT NULL COMMENT '入住单ID',
  check_in_no VARCHAR(50) NOT NULL COMMENT '入住单号',
  customer_id BIGINT NOT NULL COMMENT '客人ID',
  customer_name VARCHAR(50) NOT NULL COMMENT '客人姓名',
  customer_phone VARCHAR(20) NOT NULL COMMENT '客人手机号',
  room_type_id BIGINT NOT NULL COMMENT '房型ID',
  room_type_name VARCHAR(50) NOT NULL COMMENT '房型名称',
  check_in_date DATE NOT NULL COMMENT '入住日期',
  check_out_date DATE NOT NULL COMMENT '退房日期',
  review_code VARCHAR(16) NOT NULL COMMENT '验证码(8位随机字符)',
  review_link VARCHAR(500) NOT NULL COMMENT '评价链接',
  review_status TINYINT NOT NULL DEFAULT 0 COMMENT '评价状态:0-待评价,1-已评价',
  review_time DATETIME DEFAULT NULL COMMENT '评价时间',
  is_sent TINYINT NOT NULL DEFAULT 0 COMMENT '是否发送:0-未发送,1-已发送',
  send_time DATETIME DEFAULT NULL COMMENT '发送时间',
  send_method TINYINT DEFAULT NULL COMMENT '发送方式:1-短信,2-邮件',
  expire_time DATETIME NOT NULL COMMENT '链接过期时间',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记:0-未删除,1-已删除',
  UNIQUE KEY uk_check_in_id (check_in_id, deleted),
  UNIQUE KEY uk_review_code (review_code),
  KEY idx_customer_phone (customer_phone),
  KEY idx_review_status (review_status),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价邀请表';

-- =============================================
-- 2. 评价记录表
-- =============================================
DROP TABLE IF EXISTS review_record;
CREATE TABLE review_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  check_in_id BIGINT NOT NULL COMMENT '入住单ID',
  check_in_no VARCHAR(50) NOT NULL COMMENT '入住单号',
  invitation_id BIGINT NOT NULL COMMENT '评价邀请ID',
  customer_id BIGINT NOT NULL COMMENT '客人ID',
  customer_name VARCHAR(50) NOT NULL COMMENT '客人姓名',
  customer_phone VARCHAR(20) NOT NULL COMMENT '客人手机号',
  member_id BIGINT DEFAULT NULL COMMENT '会员ID',
  member_no VARCHAR(50) DEFAULT NULL COMMENT '会员编号',
  room_type_id BIGINT NOT NULL COMMENT '房型ID',
  room_type_name VARCHAR(50) NOT NULL COMMENT '房型名称',
  check_in_date DATE NOT NULL COMMENT '入住日期',
  check_out_date DATE NOT NULL COMMENT '退房日期',
  overall_score DECIMAL(3,1) NOT NULL COMMENT '综合评分(根据权重计算)',
  review_content VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
  selected_tags VARCHAR(500) DEFAULT NULL COMMENT '选中的评价标签ID，逗号分隔',
  is_anonymous TINYINT NOT NULL DEFAULT 0 COMMENT '是否匿名:0-否,1-是',
  review_status TINYINT NOT NULL DEFAULT 0 COMMENT '评价状态:0-待审核,1-已审核,2-已驳回',
  audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
  auditor_id BIGINT DEFAULT NULL COMMENT '审核人ID',
  auditor_name VARCHAR(50) DEFAULT NULL COMMENT '审核人姓名',
  audit_remark VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
  is_top TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶:0-否,1-是',
  top_time DATETIME DEFAULT NULL COMMENT '置顶时间',
  reply_content VARCHAR(500) DEFAULT NULL COMMENT '商家回复内容',
  reply_time DATETIME DEFAULT NULL COMMENT '回复时间',
  replied_by BIGINT DEFAULT NULL COMMENT '回复人ID',
  replied_name VARCHAR(50) DEFAULT NULL COMMENT '回复人姓名',
  like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  review_time DATETIME DEFAULT NULL COMMENT '评价时间',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记:0-未删除,1-已删除',
  UNIQUE KEY uk_check_in_id (check_in_id, deleted),
  KEY idx_customer_id (customer_id),
  KEY idx_member_id (member_id),
  KEY idx_review_status (review_status),
  KEY idx_overall_score (overall_score),
  KEY idx_review_time (review_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价记录表';

-- =============================================
-- 3. 评价指标评分表
-- =============================================
DROP TABLE IF EXISTS review_metric_score;
CREATE TABLE review_metric_score (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  review_record_id BIGINT NOT NULL COMMENT '评价记录ID',
  metric_id BIGINT NOT NULL COMMENT '评价指标ID',
  metric_name VARCHAR(50) NOT NULL COMMENT '评价指标名称',
  score INT NOT NULL COMMENT '评分(1-5分)',
  weight INT NOT NULL DEFAULT 0 COMMENT '权重百分比(0-100)',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  KEY idx_review_record_id (review_record_id),
  KEY idx_metric_id (metric_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价指标评分表';

-- =============================================
-- 4. 评价图片表
-- =============================================
DROP TABLE IF EXISTS review_image;
CREATE TABLE review_image (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  review_record_id BIGINT NOT NULL COMMENT '评价记录ID',
  image_url VARCHAR(500) NOT NULL COMMENT '图片URL',
  image_name VARCHAR(200) DEFAULT NULL COMMENT '图片名称',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  KEY idx_review_record_id (review_record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价图片表';

-- =============================================
-- 5. 评价系统菜单 (ID range: 1430-1449)
-- =============================================
-- 评价邀请管理
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1430, '评价邀请管理', 1400, 3, '/review/invitation', 'review/ReviewInvitationManage', 'review:invitation:list', 1, 1, 1, 'Message');

-- =============================================
-- 6. 按钮权限 - 评价邀请
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1431, '邀请查询', 1430, 1, '', NULL, 'review:invitation:query', 2, 1, 1, NULL),
(1432, '创建邀请', 1430, 2, '', NULL, 'review:invitation:add', 2, 1, 1, NULL),
(1433, '复制链接', 1430, 3, '', NULL, 'review:invitation:copy', 2, 1, 1, NULL),
(1434, '发送邀请', 1430, 4, '', NULL, 'review:invitation:send', 2, 1, 1, NULL),
(1435, '查看评价', 1430, 5, '', NULL, 'review:invitation:viewReview', 2, 1, 1, NULL);

-- =============================================
-- 7. 分配角色权限 - 超级管理员(role_id=1)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1430), (1, 1431), (1, 1432), (1, 1433), (1, 1434), (1, 1435);

-- =============================================
-- 8. 分配角色权限 - 酒店管理员(role_id=3): 所有评价邀请权限
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1430), (3, 1431), (3, 1432), (3, 1433), (3, 1434), (3, 1435);

-- =============================================
-- 9. 分配角色权限 - 前厅部经理(role_id=4): 查看+创建+复制
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 1430), (4, 1431), (4, 1432), (4, 1433);

-- =============================================
-- 10. 分配角色权限 - 客服人员(role_id=12): 查看+复制+发送
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(12, 1430), (12, 1431), (12, 1433), (12, 1434);

-- =============================================
-- 11. 分配角色权限 - 前台员工(role_id=6): 查看+创建
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(6, 1430), (6, 1431), (6, 1432);

-- =============================================
-- 12. 预置测试数据 - 评价邀请
-- =============================================
INSERT INTO review_invitation (check_in_id, check_in_no, customer_id, customer_name, customer_phone, room_type_id, room_type_name, check_in_date, check_out_date, review_code, review_link, review_status, review_time, is_sent, send_time, send_method, expire_time, create_time) VALUES
(1, 'CI202606150001', 1, '张三', '13800138001', 1, '豪华大床房', '2026-06-10', '2026-06-12', 'a1b2c3d4', '/h5/review?order=CI202606150001&code=a1b2c3d4', 0, NULL, 1, '2026-06-12 12:30:00', 1, '2026-06-19 12:00:00', '2026-06-12 12:00:00'),
(2, 'CI202606160002', 2, '李四', '13800138002', 2, '标准双床房', '2026-06-11', '2026-06-13', 'e5f6g7h8', '/h5/review?order=CI202606160002&code=e5f6g7h8', 1, '2026-06-13 15:30:00', 1, '2026-06-13 12:30:00', 2, '2026-06-20 12:00:00', '2026-06-13 12:00:00'),
(3, 'CI202606170003', 3, '王五', '13800138003', 1, '豪华大床房', '2026-06-12', '2026-06-14', 'i9j0k1l2', '/h5/review?order=CI202606170003&code=i9j0k1l2', 0, NULL, 0, NULL, NULL, '2026-06-21 12:00:00', '2026-06-14 12:00:00'),
(4, 'CI202606180004', 4, '赵六', '13800138004', 3, '商务套房', '2026-06-13', '2026-06-15', 'm3n4o5p6', '/h5/review?order=CI202606180004&code=m3n4o5p6', 1, '2026-06-15 20:15:00', 1, '2026-06-15 13:00:00', 1, '2026-06-22 12:00:00', '2026-06-15 12:00:00'),
(5, 'CI202606190005', 5, '孙七', '13800138005', 2, '标准双床房', '2026-06-14', '2026-06-16', 'q7r8s9t0', '/h5/review?order=CI202606190005&code=q7r8s9t0', 0, NULL, 1, '2026-06-16 14:00:00', 2, '2026-06-23 12:00:00', '2026-06-16 12:00:00');

-- =============================================
-- 13. 预置测试数据 - 评价记录（已评价的）
-- =============================================
INSERT INTO review_record (check_in_id, check_in_no, invitation_id, customer_id, customer_name, customer_phone, member_id, member_no, room_type_id, room_type_name, check_in_date, check_out_date, overall_score, review_content, selected_tags, is_anonymous, review_status, review_time, create_time) VALUES
(2, 'CI202606160002', 2, 2, '李四', '13800138002', 2, 'M000002', 2, '标准双床房', '2026-06-11', '2026-06-13', 4.5, '房间很干净，服务很好，下次还会来。位置很方便，周边配套齐全。', '1,2,3,10', 0, 1, '2026-06-13 15:30:00', '2026-06-13 15:30:00'),
(4, 'CI202606180004', 4, 4, '赵六', '13800138004', 4, 'M000004', 3, '商务套房', '2026-06-13', '2026-06-15', 4.8, '酒店设施完善，前台服务热情，住宿体验很好。早餐丰盛可口，房间整洁舒适，推荐入住。', '1,3,4,9,12', 0, 1, '2026-06-15 20:15:00', '2026-06-15 20:15:00');

-- =============================================
-- 14. 预置测试数据 - 评价指标评分
-- =============================================
INSERT INTO review_metric_score (review_record_id, metric_id, metric_name, score, weight, create_time) VALUES
(1, 1, '客房卫生', 5, 20, '2026-06-13 15:30:00'),
(1, 2, '服务态度', 5, 20, '2026-06-13 15:30:00'),
(1, 3, '设施设备', 4, 15, '2026-06-13 15:30:00'),
(1, 4, '位置交通', 5, 15, '2026-06-13 15:30:00'),
(1, 5, '性价比', 4, 15, '2026-06-13 15:30:00'),
(1, 6, '早餐质量', 4, 10, '2026-06-13 15:30:00'),
(1, 7, '环境安静', 5, 5, '2026-06-13 15:30:00'),
(2, 1, '客房卫生', 5, 20, '2026-06-15 20:15:00'),
(2, 2, '服务态度', 5, 20, '2026-06-15 20:15:00'),
(2, 3, '设施设备', 5, 15, '2026-06-15 20:15:00'),
(2, 4, '位置交通', 4, 15, '2026-06-15 20:15:00'),
(2, 5, '性价比', 5, 15, '2026-06-15 20:15:00'),
(2, 6, '早餐质量', 5, 10, '2026-06-15 20:15:00'),
(2, 7, '环境安静', 5, 5, '2026-06-15 20:15:00');

-- =============================================
-- 15. 预置测试数据 - 会员评价赠送积分记录
-- =============================================
INSERT INTO member_point_log (member_id, member_no, point_type, points, balance_before, balance_after, reason_type, reason, detail, related_order_type, related_order_id, operator_id, operator_name, create_time) VALUES
(2, 'M000002', 1, 50.00, 1500.00, 1550.00, 5, '评价积分', '入住评价赠送积分', 5, 1, 1, '系统', '2026-06-13 15:30:00'),
(4, 'M000004', 1, 50.00, 2800.00, 2850.00, 5, '评价积分', '入住评价赠送积分', 5, 2, 1, '系统', '2026-06-15 20:15:00');
