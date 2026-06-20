SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE permission_system;

-- =============================================
-- 1. 积分获取规则表
-- =============================================
CREATE TABLE IF NOT EXISTS point_earn_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '规则ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_type TINYINT NOT NULL COMMENT '规则类型：1-消费积分，2-签到积分，3-评价积分，4-推荐积分，5-生日积分，6-活动积分',
    point_value DECIMAL(12,2) DEFAULT 0 COMMENT '积分值（消费积分时为每X元获得Y积分的X值）',
    point_amount DECIMAL(12,2) DEFAULT 0 COMMENT '获得积分数（消费积分时为每X元获得的Y积分；其他类型为固定积分数）',
    point_rate DECIMAL(5,2) DEFAULT 1.00 COMMENT '积分倍率（根据会员等级的倍率调整，如金卡1.5倍）',
    start_time DATETIME COMMENT '生效开始时间',
    end_time DATETIME COMMENT '生效结束时间',
    status TINYINT DEFAULT 1 COMMENT '启用状态：0-禁用，1-启用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_rule_type (rule_type),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_end_time (end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分获取规则表';

-- =============================================
-- 2. 积分消耗规则表
-- =============================================
CREATE TABLE IF NOT EXISTS point_consume_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '规则ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_type TINYINT NOT NULL COMMENT '规则类型：1-积分抵现，2-积分兑换',
    exchange_points DECIMAL(12,2) DEFAULT 0 COMMENT '兑换积分数（如100积分）',
    exchange_amount DECIMAL(12,2) DEFAULT 0 COMMENT '兑换金额/元（如10元）',
    max_points_per_use DECIMAL(12,2) DEFAULT 0 COMMENT '单次最多使用积分',
    min_order_amount DECIMAL(12,2) DEFAULT 0 COMMENT '最低消费要求（订单金额满X元）',
    deduction_cap DECIMAL(5,2) DEFAULT 100.00 COMMENT '抵扣上限（最多抵扣订单金额的X%）',
    applicable_levels VARCHAR(500) COMMENT '适用会员等级ID列表（逗号分隔，空表示全部）',
    start_time DATETIME COMMENT '生效开始时间',
    end_time DATETIME COMMENT '生效结束时间',
    status TINYINT DEFAULT 1 COMMENT '启用状态：0-禁用，1-启用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_rule_type (rule_type),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_end_time (end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分消耗规则表';

-- =============================================
-- 3. 初始化积分获取规则数据
-- =============================================
INSERT INTO point_earn_rule (rule_name, rule_type, point_value, point_amount, point_rate, status, remark) VALUES
('消费积分', 1, 1.00, 1.00, 1.00, 1, '每消费1元获得1积分，根据会员等级倍率调整'),
('签到积分', 2, 0, 10.00, 1.00, 1, '每日签到获得10积分'),
('评价积分', 3, 0, 20.00, 1.00, 1, '完成评价获得20积分'),
('推荐积分', 4, 0, 100.00, 1.00, 1, '推荐一个新会员获得100积分'),
('生日积分', 5, 0, 200.00, 1.00, 1, '生日当月赠送200积分'),
('活动积分', 6, 0, 0, 1.00, 0, '营销活动手动发放积分');

-- =============================================
-- 4. 初始化积分消耗规则数据
-- =============================================
INSERT INTO point_consume_rule (rule_name, rule_type, exchange_points, exchange_amount, max_points_per_use, min_order_amount, deduction_cap, applicable_levels, status, remark) VALUES
('积分抵现', 1, 100.00, 10.00, 5000.00, 100.00, 30.00, '', 1, '100积分抵10元，单次最多使用5000积分，最低消费100元，最多抵扣30%'),
('积分兑换', 2, 0, 0, 0, 0, 0, '', 0, '积分兑换权益（后续商城系统实现）');

-- =============================================
-- 5. 积分规则管理菜单 (ID range: 1240-1269)
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1240, '积分规则管理', 1200, 3, '/member/pointRule', 'member/PointRuleManage', 'member:pointRule:list', 1, 1, 1, 'SetUp');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1250, '积分明细', 1200, 5, '/member/points', 'member/MemberPoints', 'member:point:list', 1, 1, 1, 'Coin');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1260, '积分统计', 1200, 6, '/member/pointStatistics', 'member/PointStatistics', 'member:pointStatistics:list', 1, 1, 1, 'DataAnalysis');

-- =============================================
-- 6. 积分规则管理按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1241, '获取规则查询', 1240, 1, '', NULL, 'member:pointRule:earn:query', 2, 1, 1, NULL),
(1242, '获取规则新增', 1240, 2, '', NULL, 'member:pointRule:earn:add', 2, 1, 1, NULL),
(1243, '获取规则修改', 1240, 3, '', NULL, 'member:pointRule:earn:edit', 2, 1, 1, NULL),
(1244, '获取规则删除', 1240, 4, '', NULL, 'member:pointRule:earn:delete', 2, 1, 1, NULL),
(1245, '消耗规则查询', 1240, 5, '', NULL, 'member:pointRule:consume:query', 2, 1, 1, NULL),
(1246, '消耗规则新增', 1240, 6, '', NULL, 'member:pointRule:consume:add', 2, 1, 1, NULL),
(1247, '消耗规则修改', 1240, 7, '', NULL, 'member:pointRule:consume:edit', 2, 1, 1, NULL),
(1248, '消耗规则删除', 1240, 8, '', NULL, 'member:pointRule:consume:delete', 2, 1, 1, NULL);

-- =============================================
-- 7. 积分明细按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1251, '积分使用', 1250, 1, '', NULL, 'member:point:use', 2, 1, 1, NULL),
(1252, '积分明细导出', 1250, 2, '', NULL, 'member:point:export', 2, 1, 1, NULL);

-- =============================================
-- 8. 积分统计按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1261, '统计查询', 1260, 1, '', NULL, 'member:pointStatistics:query', 2, 1, 1, NULL);

-- =============================================
-- 9. 分配角色权限 - 超级管理员(role_id=1)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1240), (1, 1241), (1, 1242), (1, 1243), (1, 1244), (1, 1245), (1, 1246), (1, 1247), (1, 1248),
(1, 1250), (1, 1251), (1, 1252),
(1, 1260), (1, 1261);

-- =============================================
-- 10. 分配角色权限 - 酒店管理员(role_id=3): 所有积分权限
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1240), (3, 1241), (3, 1242), (3, 1243), (3, 1244), (3, 1245), (3, 1246), (3, 1247), (3, 1248),
(3, 1250), (3, 1251), (3, 1252),
(3, 1260), (3, 1261);

-- =============================================
-- 11. 分配角色权限 - 前厅部经理(role_id=4): 查看积分规则、查看统计
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 1240), (4, 1241), (4, 1245),
(4, 1250), (4, 1251),
(4, 1260), (4, 1261);

-- =============================================
-- 12. 分配角色权限 - 前台员工(role_id=6): 使用积分抵扣
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(6, 1240), (6, 1241), (6, 1245),
(6, 1250), (6, 1251);

-- =============================================
-- 13. 分配角色权限 - 客服人员(role_id=8 即customer_service): 查看积分明细，协助会员使用积分
-- 注意：role_id=8的客服角色可能不存在，此处注释掉；且role_id=6已在上面分配
-- =============================================
-- INSERT INTO sys_role_menu (role_id, menu_id) VALUES
-- (8, 1250), (8, 1251);

-- =============================================
-- 14. 分配角色权限 - 财务人员(role_id=7): 查看积分统计
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(7, 1260), (7, 1261);

-- =============================================
-- 15. 补充测试积分明细数据（消费获取、活动获取、积分使用等）
-- =============================================
INSERT INTO member_point_log (member_id, member_no, point_type, points, balance_before, balance_after, reason_type, reason, detail, related_order_type, related_order_id, operator_id, operator_name, create_time) VALUES
((SELECT id FROM member WHERE member_no = 'MB2025011500001'), 'MB2025011500001', 1, 100, 0, 100, 1, '消费赠送', '房费消费100元，基础积分100，等级倍率1.0', 1, 1, 1, '超级管理员', '2025-02-10 12:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025011500001'), 'MB2025011500001', 1, 200, 100, 300, 2, '活动赠送', '新春活动赠送积分', NULL, NULL, 1, '超级管理员', '2025-02-15 10:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025011500001'), 'MB2025011500001', 2, 50, 300, 250, 6, '积分抵现', '500积分抵扣50元', 1, 2, 1, '超级管理员', '2025-03-01 14:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025011500001'), 'MB2025011500001', 1, 50, 250, 300, 1, '消费赠送', '房费消费50元', 1, 3, 1, '超级管理员', '2025-04-20 11:00:00'),

((SELECT id FROM member WHERE member_no = 'MB2025032000002'), 'MB2025032000002', 1, 300, 0, 300, 1, '消费赠送', '房费消费300元，基础积分300，等级倍率1.2，实际360积分', 1, 4, 1, '超级管理员', '2025-04-01 10:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025032000002'), 'MB2025032000002', 1, 500, 300, 800, 1, '消费赠送', '房费消费500元，等级倍率1.2', 1, 5, 1, '超级管理员', '2025-06-15 09:30:00'),
((SELECT id FROM member WHERE member_no = 'MB2025032000002'), 'MB2025032000002', 2, 200, 800, 600, 6, '积分抵现', '2000积分抵扣200元', 1, 6, 1, '超级管理员', '2025-07-10 16:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025032000002'), 'MB2025032000002', 1, 200, 600, 800, 2, '活动赠送', '会员日活动赠送', NULL, NULL, 1, '超级管理员', '2025-08-20 10:00:00'),

((SELECT id FROM member WHERE member_no = 'MB2025051000003'), 'MB2025051000003', 1, 600, 0, 600, 1, '消费赠送', '房费消费500元，银卡1.2倍', 1, 7, 1, '超级管理员', '2025-06-01 11:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025051000003'), 'MB2025051000003', 1, 240, 600, 840, 1, '消费赠送', '房费消费200元，银卡1.2倍', 1, 8, 1, '超级管理员', '2025-08-10 14:30:00'),
((SELECT id FROM member WHERE member_no = 'MB2025051000003'), 'MB2025051000003', 1, 100, 840, 940, 5, '推荐奖励', '推荐新会员获得100积分', NULL, NULL, 1, '超级管理员', '2025-09-05 10:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025051000003'), 'MB2025051000003', 2, 100, 940, 840, 6, '积分抵现', '1000积分抵扣100元', 1, 9, 1, '超级管理员', '2025-10-15 09:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025051000003'), 'MB2025051000003', 1, 360, 840, 1200, 1, '消费赠送', '房费消费300元，银卡1.2倍', 1, 10, 1, '超级管理员', '2026-01-20 15:00:00'),

((SELECT id FROM member WHERE member_no = 'MB2025072200004'), 'MB2025072200004', 2, 500, 8000, 7500, 6, '积分抵现', '5000积分抵扣500元', 1, 11, 1, '超级管理员', '2026-04-01 10:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025072200004'), 'MB2025072200004', 1, 1500, 7500, 9000, 1, '消费赠送', '房费消费1000元，金卡1.5倍', 1, 12, 1, '超级管理员', '2026-05-10 11:30:00'),
((SELECT id FROM member WHERE member_no = 'MB2025072200004'), 'MB2025072200004', 2, 4000, 9000, 5000, 6, '积分抵现', '40000积分抵扣4000元', 1, 13, 1, '超级管理员', '2026-06-01 14:00:00'),

((SELECT id FROM member WHERE member_no = 'MB2025090800005'), 'MB2025090800005', 1, 3000, 0, 3000, 1, '消费赠送', '房费消费2000元，金卡1.5倍', 1, 14, 1, '超级管理员', '2025-10-01 10:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025090800005'), 'MB2025090800005', 1, 4500, 3000, 7500, 1, '消费赠送', '房费消费3000元，金卡1.5倍', 1, 15, 1, '超级管理员', '2025-12-15 09:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025090800005'), 'MB2025090800005', 2, 2000, 7500, 5500, 6, '积分抵现', '20000积分抵扣2000元', 1, 16, 1, '超级管理员', '2026-02-20 14:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025090800005'), 'MB2025090800005', 1, 750, 5500, 6250, 2, '活动赠送', '周年庆活动赠送', NULL, NULL, 1, '超级管理员', '2026-04-10 10:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025090800005'), 'MB2025090800005', 1, 1750, 6250, 8000, 1, '消费赠送', '房费消费约1166元，金卡1.5倍', 1, 17, 1, '超级管理员', '2026-06-05 11:00:00'),

((SELECT id FROM member WHERE member_no = 'MB2025113000006'), 'MB2025113000006', 2, 10000, 50000, 40000, 6, '积分抵现', '100000积分抵扣10000元', 1, 18, 1, '超级管理员', '2026-04-15 10:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025113000006'), 'MB2025113000006', 2, 10000, 40000, 30000, 7, '积分兑换', '积分兑换免费升级券', NULL, NULL, 1, '超级管理员', '2026-05-20 14:00:00');
