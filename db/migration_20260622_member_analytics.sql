SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE permission_system;

-- =============================================
-- 会员深度分析模块 - 菜单与权限配置
-- =============================================

-- =============================================
-- 1. 会员数据分析菜单 (ID range: 1260-1299)
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1260, '数据分析', 1200, 5, NULL, NULL, '', 1, 1, 1, 'DataAnalysis');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1261, '会员价值分析', 1260, 1, '/member/analytics/value', 'member/MemberValueAnalysis', 'member:analytics:value', 1, 1, 1, 'Coin');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1262, '等级分布分析', 1260, 2, '/member/analytics/level', 'member/LevelDistributionAnalysis', 'member:analytics:level', 1, 1, 1, 'TrendCharts');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1263, '会员行为分析', 1260, 3, '/member/analytics/behavior', 'member/MemberBehaviorAnalysis', 'member:analytics:behavior', 1, 1, 1, 'User');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1264, '会员流失预警', 1260, 4, '/member/analytics/churn', 'member/MemberChurnWarning', 'member:analytics:churn', 1, 1, 1, 'Warning');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1265, '权益使用统计', 1260, 5, '/member/analytics/benefit', 'member/BenefitUsageStatistics', 'member:analytics:benefit', 1, 1, 1, 'Tickets');

-- =============================================
-- 2. 按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1271, '价值分析查询', 1261, 1, '', NULL, 'member:analytics:value:query', 2, 1, 1, NULL);

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1272, '等级分析查询', 1262, 1, '', NULL, 'member:analytics:level:query', 2, 1, 1, NULL);

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1273, '行为分析查询', 1263, 1, '', NULL, 'member:analytics:behavior:query', 2, 1, 1, NULL);

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1274, '流失预警查询', 1264, 1, '', NULL, 'member:analytics:churn:query', 2, 1, 1, NULL);

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1275, '流失挽留操作', 1264, 2, '', NULL, 'member:analytics:churn:retain', 2, 1, 1, NULL);

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1276, '权益统计查询', 1265, 1, '', NULL, 'member:analytics:benefit:query', 2, 1, 1, NULL);

-- =============================================
-- 3. 分配角色权限 - 超级管理员(role_id=1)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1260), (1, 1261), (1, 1262), (1, 1263), (1, 1264), (1, 1265),
(1, 1271), (1, 1272), (1, 1273), (1, 1274), (1, 1275), (1, 1276);

-- =============================================
-- 4. 分配角色权限 - 酒店管理员(role_id=3): 所有分析报表
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1260), (3, 1261), (3, 1262), (3, 1263), (3, 1264), (3, 1265),
(3, 1271), (3, 1272), (3, 1273), (3, 1274), (3, 1275), (3, 1276);

-- =============================================
-- 5. 分配角色权限 - 前厅部经理(role_id=4): 会员分析、权益统计、流失预警
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 1260), (4, 1261), (4, 1262), (4, 1264), (4, 1265),
(4, 1271), (4, 1272), (4, 1274), (4, 1275), (4, 1276);

-- =============================================
-- 6. 分配角色权限 - 财务人员(role_id=5): 权益成本分析
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(5, 1260), (5, 1261), (5, 1265),
(5, 1271), (5, 1276);

-- =============================================
-- 7. 分配角色权限 - 客服人员(role_id=7): 流失预警、会员挽留
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(7, 1260), (7, 1264),
(7, 1274), (7, 1275);

-- =============================================
-- 8. 分配角色权限 - 市场部经理(role_id=8): 会员价值、行为分析
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(8, 1260), (8, 1261), (8, 1263),
(8, 1271), (8, 1273);
