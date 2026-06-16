SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE permission_system;

-- =============================================
-- 1. 客户标签库表
-- =============================================
CREATE TABLE IF NOT EXISTS customer_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '标签ID',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名称',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE INDEX idx_tag_name (tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户标签库表';

-- =============================================
-- 2. 客户标签关联表
-- =============================================
CREATE TABLE IF NOT EXISTS customer_tag_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX idx_customer_tag (customer_id, tag_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户标签关联表';

-- =============================================
-- 3. 客户偏好表
-- =============================================
CREATE TABLE IF NOT EXISTS customer_preference (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    customer_id BIGINT NOT NULL UNIQUE COMMENT '客户ID',
    preferred_room_type VARCHAR(100) COMMENT '偏好房型（逗号分隔）',
    preferred_floor VARCHAR(50) COMMENT '楼层偏好：high-高楼层，low-低楼层，mid-中间',
    preferred_orientation VARCHAR(50) COMMENT '朝向偏好（逗号分隔）：东/南/西/北/东南/东北/西南/西北',
    preferred_bed_type VARCHAR(50) COMMENT '床型偏好：single-单人床，king-大床，twin-双床',
    preferred_view VARCHAR(100) COMMENT '景观偏好（逗号分隔）：江景/海景/山景/园景/城景',
    special_needs VARCHAR(500) COMMENT '特殊需求（逗号分隔）：no_smoke/quiet/away_elevator/near_elevator/extra_bed/baby_cot/pollen_allergy/humidifier/extra_pillow/low_floor',
    service_preference VARCHAR(500) COMMENT '服务偏好（逗号分隔）：do_not_disturb/early_newspaper/laundry/wake_up_call/early_checkin/late_checkout',
    diet_vegetarian TINYINT DEFAULT 0 COMMENT '素食：0-否，1-是',
    diet_halal TINYINT DEFAULT 0 COMMENT '清真：0-否，1-是',
    diet_seafood_allergy TINYINT DEFAULT 0 COMMENT '海鲜过敏：0-否，1-是',
    diet_no_spicy TINYINT DEFAULT 0 COMMENT '不吃辣：0-否，1-是',
    diet_other_allergy VARCHAR(200) COMMENT '其他过敏（文本输入）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_customer_id (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户偏好表';

-- =============================================
-- 4. 客户备注表
-- =============================================
CREATE TABLE IF NOT EXISTS customer_note (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '备注ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    content TEXT NOT NULL COMMENT '备注内容',
    importance TINYINT DEFAULT 1 COMMENT '重要程度：1-普通，2-重要，3-紧急',
    is_pinned TINYINT DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
    attachments VARCHAR(2000) COMMENT '附件（JSON数组，最多3个）',
    mention_roles VARCHAR(200) COMMENT '@角色（预留，逗号分隔）',
    operator_id BIGINT COMMENT '记录人ID',
    operator_name VARCHAR(50) COMMENT '记录人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_customer_id (customer_id),
    INDEX idx_importance (importance),
    INDEX idx_is_pinned (is_pinned),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户备注表';

-- =============================================
-- 5. 客户黑名单表
-- =============================================
CREATE TABLE IF NOT EXISTS customer_blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    reason TINYINT NOT NULL COMMENT '原因：1-拖欠房费，2-破坏设施，3-扰乱秩序，4-无理投诉，5-违法行为',
    detail_description TEXT NOT NULL COMMENT '详细描述',
    evidence_materials VARCHAR(2000) COMMENT '证据材料（JSON数组，最多5个）',
    blacklist_type TINYINT NOT NULL COMMENT '类型：1-临时，2-永久',
    expire_time DATETIME COMMENT '到期时间（临时时）',
    status TINYINT DEFAULT 1 COMMENT '状态：1-待审批，2-审批通过生效，3-审批拒绝，4-已解除，5-待解除',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    applicant_name VARCHAR(50) NOT NULL COMMENT '申请人姓名',
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    approver_id BIGINT COMMENT '审批人ID',
    approver_name VARCHAR(50) COMMENT '审批人姓名',
    approve_time DATETIME COMMENT '审批时间',
    approve_opinion TEXT COMMENT '审批意见',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    remove_reason VARCHAR(500) COMMENT '解除原因',
    remove_applicant_id BIGINT COMMENT '解除申请人ID',
    remove_applicant_name VARCHAR(50) COMMENT '解除申请人姓名',
    remove_apply_time DATETIME COMMENT '解除申请时间',
    remove_approver_id BIGINT COMMENT '解除审批人ID',
    remove_approver_name VARCHAR(50) COMMENT '解除审批人姓名',
    remove_approve_time DATETIME COMMENT '解除审批时间',
    remove_approve_opinion TEXT COMMENT '解除审批意见',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status),
    INDEX idx_blacklist_type (blacklist_type),
    INDEX idx_reason (reason),
    INDEX idx_apply_time (apply_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户黑名单表';

-- =============================================
-- 6. 客户合并记录表
-- =============================================
CREATE TABLE IF NOT EXISTS customer_merge_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    source_customer_id BIGINT NOT NULL COMMENT '被合并客户ID',
    target_customer_id BIGINT NOT NULL COMMENT '保留客户ID',
    source_customer_name VARCHAR(50) COMMENT '被合并客户姓名',
    target_customer_name VARCHAR(50) COMMENT '保留客户姓名',
    merge_details TEXT COMMENT '合并详情（JSON，记录字段选择）',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(50) NOT NULL COMMENT '操作人姓名',
    merge_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '合并时间',
    INDEX idx_source_customer_id (source_customer_id),
    INDEX idx_target_customer_id (target_customer_id),
    INDEX idx_merge_time (merge_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户合并记录表';

-- =============================================
-- 7. 增强客户操作日志表（新增字段）
-- =============================================
ALTER TABLE customer_operation_log ADD COLUMN change_field VARCHAR(100) COMMENT '变更字段';
ALTER TABLE customer_operation_log ADD COLUMN old_value TEXT COMMENT '原值';
ALTER TABLE customer_operation_log ADD COLUMN new_value TEXT COMMENT '新值';
ALTER TABLE customer_operation_log ADD COLUMN operator_ip VARCHAR(50) COMMENT '操作IP';

-- 扩展操作类型：
-- 1-创建客户，2-修改客户信息，3-冻结客户，4-解冻客户
-- 5-添加标签，6-移除标签
-- 7-修改偏好
-- 8-添加备注
-- 9-加入黑名单，10-解除黑名单
-- 11-客户合并
-- 12-导入客户
-- 13-导出客户

-- =============================================
-- 8. 客户表增加黑名单状态
-- =============================================
-- status: 1-正常，2-冻结，3-黑名单
-- 已有status字段，无需修改结构，只需扩展状态值

-- =============================================
-- 9. 新增角色：客服人员
-- =============================================
INSERT INTO sys_role (id, role_name, role_key, order_num, status, remark) VALUES
(12, '客服人员', 'service_staff', 12, 1, '客服人员，可管理标签/偏好/备注，提交黑名单申请，使用查重工具');

INSERT INTO sys_user (id, username, password, nickname, email, phone, status) VALUES
(13, 'service_staff', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtE/ETXmB5nNiHxqHnHfgVd5GK6', '客服人员', 'service_staff@example.com', '13800138012', 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES
(13, 12);

INSERT INTO sys_data_permission (role_id, scope_type, custom_dept_ids) VALUES
(12, 1, NULL);

-- =============================================
-- 10. 客户档案增强菜单 (ID range: 320-399)
-- =============================================

-- 标签管理
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(320, '标签管理', 300, 5, '/customer/tags', 'customer/TagManagement', 'customer:tag:list', 1, 1, 1, 'PriceTag');

-- 黑名单管理
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(321, '黑名单管理', 300, 6, '/customer/blacklist', 'customer/BlacklistManagement', 'customer:blacklist:list', 1, 1, 1, 'Warning');

-- 黑名单审批
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(322, '黑名单审批', 300, 7, '/customer/blacklistApproval', 'customer/BlacklistApproval', 'customer:blacklist:approve', 1, 1, 1, 'Checked');

-- 客户合并
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(323, '客户合并', 300, 8, '/customer/merge', 'customer/CustomerMerge', 'customer:merge', 1, 1, 1, 'CopyDocument');

-- 批量导入
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(324, '批量导入', 300, 9, '/customer/import', 'customer/CustomerImport', 'customer:import', 1, 1, 1, 'Upload');

-- 按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(330, '标签库管理', 320, 1, '', NULL, 'customer:tag:manage', 2, 1, 1, NULL),
(331, '标签查看', 320, 2, '', NULL, 'customer:tag:query', 2, 1, 1, NULL),
(332, '为客户打标签', 320, 3, '', NULL, 'customer:tag:assign', 2, 1, 1, NULL),
(333, '偏好管理', 303, 1, '', NULL, 'customer:preference:manage', 2, 1, 1, NULL),
(334, '偏好查看', 303, 2, '', NULL, 'customer:preference:query', 2, 1, 1, NULL),
(335, '备注添加', 303, 3, '', NULL, 'customer:note:add', 2, 1, 1, NULL),
(336, '备注查看', 303, 4, '', NULL, 'customer:note:query', 2, 1, 1, NULL),
(337, '黑名单提交', 321, 1, '', NULL, 'customer:blacklist:submit', 2, 1, 1, NULL),
(338, '黑名单审批', 321, 2, '', NULL, 'customer:blacklist:approve', 2, 1, 1, NULL),
(339, '黑名单解除', 321, 3, '', NULL, 'customer:blacklist:remove', 2, 1, 1, NULL),
(340, '黑名单导出', 321, 4, '', NULL, 'customer:blacklist:export', 2, 1, 1, NULL),
(341, '客户查重', 301, 9, '', NULL, 'customer:dedup', 2, 1, 1, NULL),
(342, '客户合并', 323, 1, '', NULL, 'customer:merge', 2, 1, 1, NULL),
(343, '客户导入', 324, 1, '', NULL, 'customer:import', 2, 1, 1, NULL),
(344, '客户导出', 301, 10, '', NULL, 'customer:export', 2, 1, 1, NULL),
(345, '操作日志查看', 303, 5, '', NULL, 'customer:log:query', 2, 1, 1, NULL);

-- =============================================
-- 11. 角色权限分配
-- =============================================

-- 超级管理员：所有新权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 320), (1, 321), (1, 322), (1, 323), (1, 324),
(1, 330), (1, 331), (1, 332), (1, 333), (1, 334), (1, 335), (1, 336),
(1, 337), (1, 338), (1, 339), (1, 340), (1, 341), (1, 342), (1, 343), (1, 344), (1, 345);

-- 酒店管理员(hotel_admin)：所有新权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 320), (3, 321), (3, 322), (3, 323), (3, 324),
(3, 330), (3, 331), (3, 332), (3, 333), (3, 334), (3, 335), (3, 336),
(3, 337), (3, 338), (3, 339), (3, 340), (3, 341), (3, 342), (3, 343), (3, 344), (3, 345);

-- 前台员工(receptionist)：添加标签、记录偏好、添加备注、查看标签和偏好
-- 看不到黑名单详情，不能导入导出、不能合并
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(6, 320),
(6, 331), (6, 332), (6, 333), (6, 334), (6, 335), (6, 336), (6, 345);

-- 客服主管(service_manager)：管理标签库、管理标签/偏好/备注、提交黑名单申请、查重工具、导出、不能合并
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(11, 320), (11, 321), (11, 324),
(11, 330), (11, 331), (11, 332), (11, 333), (11, 334), (11, 335), (11, 336),
(11, 337), (11, 341), (11, 343), (11, 344), (11, 345);

-- 客服人员(service_staff)：管理标签库、管理标签/偏好/备注、提交黑名单申请、查重工具、导出、不能合并
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(12, 300), (12, 301), (12, 302), (12, 303), (12, 304),
(12, 311), (12, 312), (12, 313),
(12, 320), (12, 321), (12, 324),
(12, 330), (12, 331), (12, 332), (12, 333), (12, 334), (12, 335), (12, 336),
(12, 337), (12, 341), (12, 343), (12, 344), (12, 345);

-- 前厅部经理(frontdesk_manager)：审批黑名单、客户合并、导入导出
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 320), (4, 321), (4, 322), (4, 323), (4, 324),
(4, 330), (4, 331), (4, 332), (4, 333), (4, 334), (4, 335), (4, 336),
(4, 337), (4, 338), (4, 339), (4, 340), (4, 341), (4, 342), (4, 343), (4, 344), (4, 345);
