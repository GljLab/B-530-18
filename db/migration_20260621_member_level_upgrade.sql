SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE permission_system;

-- =============================================
-- 1. 给 member_level_log 表增加触发方式字段
-- =============================================
ALTER TABLE member_level_log
ADD COLUMN trigger_type TINYINT DEFAULT 1 COMMENT '触发方式：1-系统自动，2-管理员手动' AFTER operator_name;

ALTER TABLE member_level_log ADD INDEX idx_trigger_type (trigger_type);

-- 补全历史数据的 trigger_type
UPDATE member_level_log SET trigger_type = 2 WHERE operator_id IS NOT NULL;
UPDATE member_level_log SET trigger_type = 1 WHERE operator_id IS NULL;

-- =============================================
-- 2. 定时任务执行日志表
-- =============================================
CREATE TABLE IF NOT EXISTS member_level_task_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    task_type TINYINT NOT NULL COMMENT '任务类型：1-升级检查，2-保级降级检查，3-年度消费重置',
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    execute_time DATETIME NOT NULL COMMENT '执行时间',
    execute_result TINYINT NOT NULL DEFAULT 1 COMMENT '执行结果：1-成功，2-失败',
    process_count INT DEFAULT 0 COMMENT '处理会员数',
    upgrade_count INT DEFAULT 0 COMMENT '升级人数',
    downgrade_count INT DEFAULT 0 COMMENT '降级人数',
    reset_count INT DEFAULT 0 COMMENT '重置人数',
    error_msg TEXT COMMENT '异常信息',
    operator_id BIGINT COMMENT '操作人ID(手动触发时)',
    operator_name VARCHAR(50) COMMENT '操作人姓名(手动触发时)',
    trigger_type TINYINT DEFAULT 1 COMMENT '触发方式：1-系统定时，2-手动触发',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_task_type (task_type),
    INDEX idx_execute_time (execute_time),
    INDEX idx_execute_result (execute_result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员等级定时任务执行日志';

-- =============================================
-- 3. 升降级统计菜单 (ID range: 1280-1289)
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1280, '升降级统计', 1200, 8, '/member/levelChangeStatistics', 'member/LevelChangeStatistics', 'member:levelChange:statistics', 1, 1, 1, 'TrendCharts');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1281, '统计查询', 1280, 1, '', NULL, 'member:levelChange:query', 2, 1, 1, NULL);

-- =============================================
-- 4. 定时任务管理菜单 (ID range: 1290-1299)
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1290, '定时任务管理', 1200, 9, '/member/taskManage', 'member/TaskManage', 'member:task:list', 1, 1, 1, 'Timer');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1291, '任务查询', 1290, 1, '', NULL, 'member:task:query', 2, 1, 1, NULL),
(1292, '手动触发', 1290, 2, '', NULL, 'member:task:trigger', 2, 1, 1, NULL);

-- =============================================
-- 5. 升降级记录全局查询菜单 (ID range: 1300-1309)
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1300, '等级变更记录', 1200, 10, '/member/levelChangeLog', 'member/LevelChangeLog', 'member:levelLog:list', 1, 1, 1, 'Tickets');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1301, '记录查询', 1300, 1, '', NULL, 'member:levelLog:query', 2, 1, 1, NULL);

-- =============================================
-- 6. 分配角色权限 - 超级管理员(role_id=1)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1280), (1, 1281),
(1, 1290), (1, 1291), (1, 1292),
(1, 1300), (1, 1301);

-- =============================================
-- 7. 分配角色权限 - 酒店管理员(role_id=3): 所有权限
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1280), (3, 1281),
(3, 1290), (3, 1291), (3, 1292),
(3, 1300), (3, 1301);

-- =============================================
-- 8. 分配角色权限 - 前厅部经理(role_id=4): 查看统计和记录，不可触发任务
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 1280), (4, 1281),
(4, 1300), (4, 1301);

-- =============================================
-- 9. 分配角色权限 - 客服人员(role_id=5): 查看会员等级变更历史(已有member:query)
-- =============================================
-- 客服人员已通过 member:query 权限查看会员详情页中的等级变更记录

-- =============================================
-- 10. 分配角色权限 - 前台员工(role_id=6): 仅查看会员当前等级，无权查看升降级记录
--     (不分配新权限)
-- =============================================
