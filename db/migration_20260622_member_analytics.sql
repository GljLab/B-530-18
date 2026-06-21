SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE permission_system;

-- =============================================
-- 会员深度分析模块 - 菜单与权限配置
-- =============================================

-- =============================================
-- 1. 创建市场部经理角色 (role_id=13)
-- =============================================
INSERT INTO sys_role (id, role_name, role_key, order_num, status, remark) VALUES
(13, '市场部经理', 'marketing_manager', 13, 1, '市场部经理，查看会员价值分析和行为分析，制定营销策略');

INSERT INTO sys_user (id, username, password, nickname, email, phone, status) VALUES
(14, 'marketing_manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtE/ETXmB5nNiHxqHnHfgVd5GK6', '市场部经理', 'marketing@example.com', '13800138013', 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES
(14, 13);

INSERT INTO sys_data_permission (role_id, scope_type, custom_dept_ids) VALUES
(13, 1, NULL);

-- =============================================
-- 2. 会员数据分析菜单 (ID range: 1310-1349)
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1310, '数据分析', 1200, 5, NULL, NULL, '', 1, 1, 1, 'DataAnalysis');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1311, '会员价值分析', 1310, 1, '/member/analytics/value', 'member/MemberValueAnalysis', 'member:analytics:value', 1, 1, 1, 'Coin');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1312, '等级分布分析', 1310, 2, '/member/analytics/level', 'member/LevelDistributionAnalysis', 'member:analytics:level', 1, 1, 1, 'TrendCharts');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1313, '会员行为分析', 1310, 3, '/member/analytics/behavior', 'member/MemberBehaviorAnalysis', 'member:analytics:behavior', 1, 1, 1, 'User');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1314, '会员流失预警', 1310, 4, '/member/analytics/churn', 'member/MemberChurnWarning', 'member:analytics:churn', 1, 1, 1, 'Warning');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1315, '权益使用统计', 1310, 5, '/member/analytics/benefit', 'member/BenefitUsageStatistics', 'member:analytics:benefit', 1, 1, 1, 'Tickets');

-- =============================================
-- 3. 按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1321, '价值分析查询', 1311, 1, '', NULL, 'member:analytics:value:query', 2, 1, 1, NULL);

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1322, '等级分析查询', 1312, 1, '', NULL, 'member:analytics:level:query', 2, 1, 1, NULL);

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1323, '行为分析查询', 1313, 1, '', NULL, 'member:analytics:behavior:query', 2, 1, 1, NULL);

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1324, '流失预警查询', 1314, 1, '', NULL, 'member:analytics:churn:query', 2, 1, 1, NULL);

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1325, '流失挽留操作', 1314, 2, '', NULL, 'member:analytics:churn:retain', 2, 1, 1, NULL);

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1326, '权益统计查询', 1315, 1, '', NULL, 'member:analytics:benefit:query', 2, 1, 1, NULL);

-- =============================================
-- 4. 分配角色权限 - 超级管理员(role_id=1)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1310), (1, 1311), (1, 1312), (1, 1313), (1, 1314), (1, 1315),
(1, 1321), (1, 1322), (1, 1323), (1, 1324), (1, 1325), (1, 1326);

-- =============================================
-- 5. 分配角色权限 - 酒店管理员(role_id=3): 所有分析报表
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1310), (3, 1311), (3, 1312), (3, 1313), (3, 1314), (3, 1315),
(3, 1321), (3, 1322), (3, 1323), (3, 1324), (3, 1325), (3, 1326);

-- =============================================
-- 6. 分配角色权限 - 前厅部经理(role_id=4): 会员分析、权益统计、流失预警
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 1310), (4, 1311), (4, 1312), (4, 1314), (4, 1315),
(4, 1321), (4, 1322), (4, 1324), (4, 1325), (4, 1326);

-- =============================================
-- 7. 分配角色权限 - 财务人员(role_id=7): 权益成本分析
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(7, 1310), (7, 1311), (7, 1315),
(7, 1321), (7, 1326);

-- =============================================
-- 8. 分配角色权限 - 客服人员(role_id=12): 流失预警、会员挽留
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(12, 1310), (12, 1314),
(12, 1324), (12, 1325);

-- =============================================
-- 9. 分配角色权限 - 市场部经理(role_id=13): 会员价值、行为分析
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(13, 1310), (13, 1311), (13, 1313),
(13, 1321), (13, 1323);
