SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE permission_system;

-- =============================================
-- 1. 会员等级表
-- =============================================
CREATE TABLE IF NOT EXISTS member_level (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '等级ID',
    level_name VARCHAR(50) NOT NULL COMMENT '等级名称',
    level_code VARCHAR(50) NOT NULL UNIQUE COMMENT '等级编码',
    level_icon VARCHAR(500) COMMENT '等级图标',
    level_color VARCHAR(20) COMMENT '等级颜色(主题色)',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '等级排序(数字越大等级越高)',
    upgrade_type TINYINT NOT NULL DEFAULT 1 COMMENT '升级条件类型：1-消费金额，2-积分',
    upgrade_condition DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '升级条件值',
    keep_condition DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '保级条件(年度消费)',
    room_discount DECIMAL(5,2) DEFAULT 100.00 COMMENT '房费折扣(%)',
    dining_discount DECIMAL(5,2) DEFAULT 100.00 COMMENT '餐饮折扣(%)',
    point_rate DECIMAL(5,2) DEFAULT 1.00 COMMENT '积分倍率',
    deposit_reduction DECIMAL(5,2) DEFAULT 0 COMMENT '押金减免比例(%)',
    services TEXT COMMENT '专属服务(JSON数组)',
    other_benefits TEXT COMMENT '其他权益(JSON数组)',
    status TINYINT DEFAULT 1 COMMENT '启用状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_sort_order (sort_order),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员等级表';

-- =============================================
-- 2. 会员表
-- =============================================
CREATE TABLE IF NOT EXISTS member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '会员ID',
    member_no VARCHAR(32) NOT NULL UNIQUE COMMENT '会员卡号',
    customer_id BIGINT NOT NULL UNIQUE COMMENT '关联客户ID',
    customer_name VARCHAR(50) COMMENT '客户姓名',
    phone VARCHAR(20) COMMENT '手机号',
    level_id BIGINT NOT NULL COMMENT '当前等级ID',
    level_name VARCHAR(50) COMMENT '当前等级名称',
    register_source TINYINT DEFAULT 1 COMMENT '注册来源：1-前台，2-官网，3-APP',
    referrer_id BIGINT COMMENT '推荐人会员ID',
    referrer_no VARCHAR(32) COMMENT '推荐人会员卡号',
    total_points DECIMAL(12,2) DEFAULT 0 COMMENT '累计积分',
    current_points DECIMAL(12,2) DEFAULT 0 COMMENT '当前积分(可用)',
    total_spent DECIMAL(12,2) DEFAULT 0 COMMENT '累计消费金额',
    yearly_spent DECIMAL(12,2) DEFAULT 0 COMMENT '本年度消费金额',
    stay_count INT DEFAULT 0 COMMENT '入住次数',
    last_stay_time DATETIME COMMENT '上次入住时间',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已冻结，1-正常',
    freeze_reason VARCHAR(500) COMMENT '冻结原因',
    freeze_time DATETIME COMMENT '冻结时间',
    freeze_operator_id BIGINT COMMENT '冻结操作人ID',
    freeze_operator_name VARCHAR(50) COMMENT '冻结操作人姓名',
    register_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_member_no (member_no),
    INDEX idx_customer_id (customer_id),
    INDEX idx_phone (phone),
    INDEX idx_level_id (level_id),
    INDEX idx_status (status),
    INDEX idx_register_time (register_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员表';

-- =============================================
-- 3. 积分明细表
-- =============================================
CREATE TABLE IF NOT EXISTS member_point_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    member_no VARCHAR(32) COMMENT '会员卡号',
    point_type TINYINT NOT NULL COMMENT '类型：1-获取，2-使用',
    points DECIMAL(12,2) NOT NULL COMMENT '积分数量',
    balance_before DECIMAL(12,2) COMMENT '变动前余额',
    balance_after DECIMAL(12,2) COMMENT '变动后余额',
    reason_type TINYINT COMMENT '原因类型：1-消费赠送，2-活动赠送，3-客诉补偿，4-生日礼遇，5-推荐奖励，6-积分兑换，7-其他',
    reason VARCHAR(200) COMMENT '原因说明',
    detail TEXT COMMENT '详细说明',
    related_order_type TINYINT COMMENT '关联单类型：1-入住单，2-预订单',
    related_order_id BIGINT COMMENT '关联单ID',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_member_id (member_id),
    INDEX idx_member_no (member_no),
    INDEX idx_point_type (point_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分明细表';

-- =============================================
-- 4. 等级变更记录表
-- =============================================
CREATE TABLE IF NOT EXISTS member_level_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    member_no VARCHAR(32) COMMENT '会员卡号',
    change_type TINYINT NOT NULL COMMENT '变更类型：1-升级，2-降级，3-手动调整',
    old_level_id BIGINT COMMENT '原等级ID',
    old_level_name VARCHAR(50) COMMENT '原等级名称',
    new_level_id BIGINT NOT NULL COMMENT '新等级ID',
    new_level_name VARCHAR(50) COMMENT '新等级名称',
    reason VARCHAR(500) COMMENT '变更原因',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_member_id (member_id),
    INDEX idx_member_no (member_no),
    INDEX idx_change_type (change_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='等级变更记录表';

-- =============================================
-- 5. 初始化会员等级数据
-- =============================================
INSERT INTO member_level (level_name, level_code, level_icon, level_color, sort_order, upgrade_type, upgrade_condition, keep_condition, room_discount, dining_discount, point_rate, deposit_reduction, services, other_benefits, status) VALUES
('普通会员', 'NORMAL', '👤', '#909399', 1, 1, 0, 0, 100.00, 100.00, 1.00, 0, '[]', '[]', 1),
('银卡会员', 'SILVER', '🥈', '#C0C4CC', 2, 1, 1000.00, 500.00, 95.00, 98.00, 1.20, 50.00, '["延迟退房","提前入住"]', '["房间优选"]', 1),
('金卡会员', 'GOLD', '🥇', '#E6A23C', 3, 1, 5000.00, 2000.00, 90.00, 95.00, 1.50, 100.00, '["免费升级房型","延迟退房","提前入住","免费早餐","专属客服","生日礼遇"]', '["房间优选","积分兑换优先","预订优先"]', 1),
('钻石会员', 'DIAMOND', '💎', '#409EFF', 4, 1, 20000.00, 10000.00, 85.00, 90.00, 2.00, 100.00, '["免费升级房型","延迟退房","提前入住","免费早餐","免费接送机","专属客服","生日礼遇"]', '["房间优选","积分兑换优先","预订优先"]', 1);

-- =============================================
-- 6. 会员管理菜单 (ID range: 1200-1299)
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1200, '会员管理', 0, 6, '/member', NULL, '', 1, 1, 1, 'Vip');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1210, '会员等级管理', 1200, 1, '/member/level', 'member/MemberLevelManage', 'member:level:list', 1, 1, 1, 'Medal');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1220, '会员列表', 1200, 2, '/member/list', 'member/MemberList', 'member:list', 1, 1, 1, 'User');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1221, '会员详情', 1200, 3, '/member/detail/:id', 'member/MemberDetail', 'member:query', 1, 0, 1, 'View');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1230, '会员统计', 1200, 4, '/member/statistics', 'member/MemberStatistics', 'member:statistics:list', 1, 1, 1, 'DataLine');

-- =============================================
-- 7. 会员等级管理按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1211, '等级查询', 1210, 1, '', NULL, 'member:level:query', 2, 1, 1, NULL),
(1212, '等级新增', 1210, 2, '', NULL, 'member:level:add', 2, 1, 1, NULL),
(1213, '等级修改', 1210, 3, '', NULL, 'member:level:edit', 2, 1, 1, NULL),
(1214, '等级删除', 1210, 4, '', NULL, 'member:level:delete', 2, 1, 1, NULL);

-- =============================================
-- 8. 会员管理按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1222, '会员查询', 1220, 1, '', NULL, 'member:query', 2, 1, 1, NULL),
(1223, '会员注册', 1220, 2, '', NULL, 'member:add', 2, 1, 1, NULL),
(1224, '会员编辑', 1220, 3, '', NULL, 'member:edit', 2, 1, 1, NULL),
(1225, '发放积分', 1220, 4, '', NULL, 'member:point:add', 2, 1, 1, NULL),
(1226, '调整等级', 1220, 5, '', NULL, 'member:level:adjust', 2, 1, 1, NULL),
(1227, '冻结会员', 1220, 6, '', NULL, 'member:freeze', 2, 1, 1, NULL),
(1228, '解冻会员', 1220, 7, '', NULL, 'member:unfreeze', 2, 1, 1, NULL),
(1229, '导出会员', 1220, 8, '', NULL, 'member:export', 2, 1, 1, NULL);

-- =============================================
-- 9. 会员统计按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1231, '统计查询', 1230, 1, '', NULL, 'member:statistics:query', 2, 1, 1, NULL);

-- =============================================
-- 10. 分配角色权限 - 超级管理员(role_id=1)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1200), (1, 1210), (1, 1211), (1, 1212), (1, 1213), (1, 1214),
(1, 1220), (1, 1221), (1, 1222), (1, 1223), (1, 1224), (1, 1225), (1, 1226), (1, 1227), (1, 1228), (1, 1229),
(1, 1230), (1, 1231);

-- =============================================
-- 11. 分配角色权限 - 酒店管理员(role_id=3): 所有会员权限
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1200), (3, 1210), (3, 1211), (3, 1212), (3, 1213), (3, 1214),
(3, 1220), (3, 1221), (3, 1222), (3, 1223), (3, 1224), (3, 1225), (3, 1226), (3, 1227), (3, 1228), (3, 1229),
(3, 1230), (3, 1231);

-- =============================================
-- 12. 分配角色权限 - 前厅部经理(role_id=4): 查看会员、发放积分、调整等级需审批(暂不实现审批)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 1200), (4, 1210), (4, 1211),
(4, 1220), (4, 1221), (4, 1222), (4, 1223), (4, 1224), (4, 1225), (4, 1226), (4, 1227), (4, 1228),
(4, 1230), (4, 1231);

-- =============================================
-- 13. 分配角色权限 - 前台员工(role_id=6): 注册会员、查看会员信息
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(6, 1200), (6, 1210), (6, 1211),
(6, 1220), (6, 1221), (6, 1222), (6, 1223), (6, 1224);

-- =============================================
-- 14. 分配角色权限 - 财务人员(role_id=7): 查看消费相关数据
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(7, 1200), (7, 1220), (7, 1221), (7, 1222),
(7, 1230), (7, 1231);

-- =============================================
-- 15. 测试客户数据
-- =============================================
INSERT INTO customer (name, gender, phone, id_type, id_number, customer_type, status, create_time) VALUES
('张三', 1, '13800000001', 1, '110101199001011234', 1, 1, '2025-01-15 10:00:00'),
('李四', 2, '13800000002', 1, '110101199202022345', 1, 1, '2025-03-20 14:30:00'),
('王五', 1, '13800000003', 1, '110101198803033456', 1, 1, '2025-05-10 09:15:00'),
('赵六', 2, '13800000004', 1, '110101199504044567', 1, 1, '2025-07-22 16:45:00'),
('钱七', 1, '13800000005', 1, '110101198705055678', 1, 1, '2025-09-08 11:20:00'),
('孙八', 2, '13800000006', 1, '110101199306066789', 1, 1, '2025-11-30 08:00:00'),
('周九', 1, '13800000007', 1, '110101199107077890', 1, 1, '2026-01-12 13:40:00'),
('吴十', 2, '13800000008', 1, '110101199408088901', 1, 1, '2026-02-28 15:25:00'),
('郑十一', 1, '13800000009', 1, '110101198909099012', 1, 1, '2026-04-15 10:50:00'),
('冯十二', 2, '13800000010', 1, '110101199610100123', 1, 1, '2026-06-01 09:30:00');

-- =============================================
-- 16. 测试会员数据
-- =============================================
INSERT INTO member (member_no, customer_id, customer_name, phone, level_id, level_name, register_source, total_points, current_points, total_spent, yearly_spent, stay_count, last_stay_time, status, register_time, create_time) VALUES
('MB2025011500001', (SELECT id FROM customer WHERE phone = '13800000001'), '张三', '13800000001', 1, '普通会员', 1, 500, 300, 5000.00, 2000.00, 5, '2026-05-20 12:00:00', 1, '2025-01-15 10:00:00', '2025-01-15 10:00:00'),
('MB2025032000002', (SELECT id FROM customer WHERE phone = '13800000002'), '李四', '13800000002', 2, '银卡会员', 1, 1500, 800, 15000.00, 6000.00, 12, '2026-06-10 14:00:00', 1, '2025-03-20 14:30:00', '2025-03-20 14:30:00'),
('MB2025051000003', (SELECT id FROM customer WHERE phone = '13800000003'), '王五', '13800000003', 2, '银卡会员', 2, 1800, 1200, 18000.00, 8000.00, 15, '2026-06-15 11:30:00', 1, '2025-05-10 09:15:00', '2025-05-10 09:15:00'),
('MB2025072200004', (SELECT id FROM customer WHERE phone = '13800000004'), '赵六', '13800000004', 3, '金卡会员', 1, 8000, 5000, 55000.00, 25000.00, 30, '2026-06-12 16:00:00', 1, '2025-07-22 16:45:00', '2025-07-22 16:45:00'),
('MB2025090800005', (SELECT id FROM customer WHERE phone = '13800000005'), '钱七', '13800000005', 3, '金卡会员', 3, 12000, 8000, 80000.00, 35000.00, 45, '2026-06-18 09:00:00', 1, '2025-09-08 11:20:00', '2025-09-08 11:20:00'),
('MB2025113000006', (SELECT id FROM customer WHERE phone = '13800000006'), '孙八', '13800000006', 4, '钻石会员', 1, 50000, 30000, 250000.00, 100000.00, 80, '2026-06-16 10:30:00', 1, '2025-11-30 08:00:00', '2025-11-30 08:00:00'),
('MB2026011200007', (SELECT id FROM customer WHERE phone = '13800000007'), '周九', '13800000007', 1, '普通会员', 1, 200, 150, 2000.00, 1500.00, 2, '2026-03-10 13:00:00', 1, '2026-01-12 13:40:00', '2026-01-12 13:40:00'),
('MB2026022800008', (SELECT id FROM customer WHERE phone = '13800000008'), '吴十', '13800000008', 2, '银卡会员', 2, 900, 600, 9000.00, 7000.00, 8, '2026-05-25 15:00:00', 1, '2026-02-28 15:25:00', '2026-02-28 15:25:00'),
('MB2026041500009', (SELECT id FROM customer WHERE phone = '13800000009'), '郑十一', '13800000009', 1, '普通会员', 3, 100, 100, 800.00, 800.00, 1, '2026-04-20 11:00:00', 0, '2026-04-15 10:50:00', '2026-04-15 10:50:00'),
('MB2026060100010', (SELECT id FROM customer WHERE phone = '13800000010'), '冯十二', '13800000010', 3, '金卡会员', 1, 6000, 4500, 45000.00, 20000.00, 25, '2026-06-17 14:30:00', 1, '2026-06-01 09:30:00', '2026-06-01 09:30:00');

-- =============================================
-- 17. 测试积分明细数据
-- =============================================
INSERT INTO member_point_log (member_id, member_no, point_type, points, balance_before, balance_after, reason_type, reason, operator_id, operator_name, create_time) VALUES
((SELECT id FROM member WHERE member_no = 'MB2025072200004'), 'MB2025072200004', 1, 500, 0, 500, 1, '消费赠送', 1, '超级管理员', '2025-08-01 10:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025072200004'), 'MB2025072200004', 1, 1000, 500, 1500, 1, '消费赠送', 1, '超级管理员', '2025-09-15 14:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025072200004'), 'MB2025072200004', 1, 2000, 1500, 3500, 1, '消费赠送', 1, '超级管理员', '2025-11-20 09:30:00'),
((SELECT id FROM member WHERE member_no = 'MB2025072200004'), 'MB2025072200004', 1, 500, 3500, 4000, 4, '生日礼遇', 1, '超级管理员', '2026-01-10 08:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025072200004'), 'MB2025072200004', 1, 2000, 4000, 6000, 1, '消费赠送', 1, '超级管理员', '2026-03-20 11:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025072200004'), 'MB2025072200004', 1, 2000, 6000, 8000, 2, '活动赠送', 1, '超级管理员', '2026-05-01 10:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025113000006'), 'MB2025113000006', 1, 10000, 0, 10000, 1, '消费赠送', 1, '超级管理员', '2025-12-15 10:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025113000006'), 'MB2025113000006', 1, 15000, 10000, 25000, 1, '消费赠送', 1, '超级管理员', '2026-02-20 14:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025113000006'), 'MB2025113000006', 1, 25000, 25000, 50000, 2, '周年庆活动', 1, '超级管理员', '2026-05-15 09:00:00');

-- =============================================
-- 18. 测试等级变更记录
-- =============================================
INSERT INTO member_level_log (member_id, member_no, change_type, old_level_id, old_level_name, new_level_id, new_level_name, reason, operator_id, operator_name, create_time) VALUES
((SELECT id FROM member WHERE member_no = 'MB2025072200004'), 'MB2025072200004', 1, 1, '普通会员', 2, '银卡会员', '累计消费达标自动升级', 1, '超级管理员', '2025-10-01 00:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025072200004'), 'MB2025072200004', 1, 2, '银卡会员', 3, '金卡会员', '累计消费达标自动升级', 1, '超级管理员', '2026-02-10 00:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025113000006'), 'MB2025113000006', 1, 1, '普通会员', 2, '银卡会员', '累计消费达标自动升级', 1, '超级管理员', '2026-01-05 00:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025113000006'), 'MB2025113000006', 1, 2, '银卡会员', 3, '金卡会员', '累计消费达标自动升级', 1, '超级管理员', '2026-03-20 00:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025113000006'), 'MB2025113000006', 3, 3, '金卡会员', 4, '钻石会员', 'VIP客户特批', 1, '超级管理员', '2026-05-20 10:00:00');
