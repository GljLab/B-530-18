SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE permission_system;

-- =============================================
-- 1. 扩展会员等级表，增加权益配置字段
-- =============================================
ALTER TABLE member_level
    ADD COLUMN late_checkout_time VARCHAR(10) DEFAULT '12:00' COMMENT '延迟退房时间' AFTER deposit_reduction,
    ADD COLUMN free_upgrade TINYINT DEFAULT 0 COMMENT '是否可免费升级房型：0-否，1-是' AFTER late_checkout_time,
    ADD COLUMN free_breakfast TINYINT DEFAULT 0 COMMENT '是否含免费早餐：0-否，1-是' AFTER free_upgrade,
    ADD COLUMN early_checkin_time VARCHAR(10) DEFAULT '14:00' COMMENT '提前入住时间' AFTER free_breakfast;

-- 更新现有等级数据的权益配置
UPDATE member_level SET late_checkout_time = '12:00', free_upgrade = 0, free_breakfast = 0, early_checkin_time = '14:00' WHERE level_code = 'NORMAL';
UPDATE member_level SET late_checkout_time = '14:00', free_upgrade = 0, free_breakfast = 0, early_checkin_time = '13:00' WHERE level_code = 'SILVER';
UPDATE member_level SET late_checkout_time = '14:00', free_upgrade = 1, free_breakfast = 1, early_checkin_time = '12:00' WHERE level_code = 'GOLD';
UPDATE member_level SET late_checkout_time = '16:00', free_upgrade = 1, free_breakfast = 1, early_checkin_time = '10:00' WHERE level_code = 'DIAMOND';

-- =============================================
-- 2. 权益使用记录表
-- =============================================
CREATE TABLE IF NOT EXISTS member_benefit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    member_no VARCHAR(32) COMMENT '会员卡号',
    member_name VARCHAR(50) COMMENT '会员姓名',
    benefit_type TINYINT NOT NULL COMMENT '权益类型：1-房费折扣，2-免费升级房型，3-押金减免，4-延迟退房，5-积分发放，6-提前入住，7-免费早餐，8-其他',
    benefit_type_name VARCHAR(50) COMMENT '权益类型名称',
    related_order_type TINYINT COMMENT '关联单类型：1-预订单，2-入住单',
    related_order_id BIGINT COMMENT '关联单ID',
    related_order_no VARCHAR(50) COMMENT '关联单号',
    benefit_content TEXT COMMENT '权益内容（JSON格式，存储具体优惠信息）',
    original_amount DECIMAL(12,2) COMMENT '原始金额',
    benefit_amount DECIMAL(12,2) COMMENT '优惠金额',
    actual_amount DECIMAL(12,2) COMMENT '实际金额',
    remark VARCHAR(500) COMMENT '备注',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_member_id (member_id),
    INDEX idx_member_no (member_no),
    INDEX idx_benefit_type (benefit_type),
    INDEX idx_related_order (related_order_type, related_order_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员权益使用记录表';

-- =============================================
-- 3. 扩展预订表，增加折扣相关字段
-- =============================================
ALTER TABLE booking
    ADD COLUMN member_discount DECIMAL(12,2) DEFAULT 0 COMMENT '会员折扣金额' AFTER discount,
    ADD COLUMN member_discount_rate DECIMAL(5,2) DEFAULT 100.00 COMMENT '会员折扣率(%)' AFTER member_discount,
    ADD COLUMN member_discount_remark VARCHAR(200) COMMENT '会员折扣说明' AFTER member_discount_rate;

-- =============================================
-- 4. 扩展入住表，增加权益相关字段
-- =============================================
ALTER TABLE check_in
    ADD COLUMN standard_deposit DECIMAL(12,2) DEFAULT 0 COMMENT '标准押金金额' AFTER deposit_amount,
    ADD COLUMN deposit_reduction_amount DECIMAL(12,2) DEFAULT 0 COMMENT '押金减免金额' AFTER standard_deposit,
    ADD COLUMN deposit_reduction_rate DECIMAL(5,2) DEFAULT 0 COMMENT '押金减免比例(%)' AFTER deposit_reduction_amount,
    ADD COLUMN is_upgraded TINYINT DEFAULT 0 COMMENT '是否升级房型：0-否，1-是' AFTER deposit_reduction_rate,
    ADD COLUMN original_room_type_id BIGINT COMMENT '原房型ID' AFTER is_upgraded,
    ADD COLUMN original_room_type_name VARCHAR(50) COMMENT '原房型名称' AFTER original_room_type_id,
    ADD COLUMN upgrade_reason VARCHAR(200) COMMENT '升级原因' AFTER original_room_type_name,
    ADD COLUMN is_late_checkout TINYINT DEFAULT 0 COMMENT '是否延迟退房：0-否，1-是' AFTER actual_check_out_time,
    ADD COLUMN late_checkout_reason VARCHAR(200) COMMENT '延迟退房原因' AFTER is_late_checkout;

-- =============================================
-- 5. 扩展房间表，增加优选标记字段和禁烟字段
-- =============================================
ALTER TABLE room
    ADD COLUMN is_smoking TINYINT DEFAULT 0 COMMENT '是否可吸烟：0-否，1-是' AFTER special_tags,
    ADD COLUMN is_preferred TINYINT DEFAULT 0 COMMENT '是否会员优选：0-否，1-是' AFTER is_smoking;

-- 更新部分房间为会员优选（基于景观、朝向、位置特征）
UPDATE room SET is_preferred = 1 WHERE id IN (
    SELECT id FROM (
        SELECT r.id FROM room r
        WHERE r.status = 1 AND r.deleted = 0
        AND (
            r.view_type IS NOT NULL
            OR r.orientation IN ('南','东南','西南')
            OR r.location_features LIKE '%靠近电梯%'
            OR r.location_features LIKE '%安静%'
            OR r.location_features LIKE '%景观%'
        )
        ORDER BY RAND()
        LIMIT 20
    ) AS temp
);

-- 更新部分房间为可吸烟房
UPDATE room SET is_smoking = 1 WHERE id IN (
    SELECT id FROM (
        SELECT r.id FROM room r
        WHERE r.status = 1 AND r.deleted = 0
        AND (r.special_tags LIKE '%吸烟%' OR r.special_tags LIKE '%允许宠物%')
        ORDER BY RAND()
        LIMIT 10
    ) AS temp
);

-- =============================================
-- 6. 会员权益管理菜单 (ID range: 1270-1279)
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1270, '权益使用记录', 1200, 7, '/member/benefitLog', 'member/BenefitLogList', 'member:benefit:list', 1, 1, 1, 'Tickets');

-- 权益使用记录按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1271, '权益查询', 1270, 1, '', NULL, 'member:benefit:query', 2, 1, 1, NULL),
(1272, '权益导出', 1270, 2, '', NULL, 'member:benefit:export', 2, 1, 1, NULL);

-- =============================================
-- 7. 分配角色权限 - 超级管理员(role_id=1)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1270), (1, 1271), (1, 1272);

-- =============================================
-- 8. 分配角色权限 - 酒店管理员(role_id=3)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1270), (3, 1271), (3, 1272);

-- =============================================
-- 9. 分配角色权限 - 前厅部经理(role_id=4)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 1270), (4, 1271), (4, 1272);

-- =============================================
-- 10. 分配角色权限 - 前台员工(role_id=6)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(6, 1270), (6, 1271);

-- =============================================
-- 11. 分配角色权限 - 客服人员(role_id=8)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(8, 1270), (8, 1271);

-- =============================================
-- 12. 测试权益使用记录数据
-- =============================================
INSERT INTO member_benefit_log (member_id, member_no, member_name, benefit_type, benefit_type_name, related_order_type, related_order_no, benefit_content, original_amount, benefit_amount, actual_amount, remark, operator_id, operator_name, create_time) VALUES
((SELECT id FROM member WHERE member_no = 'MB2025072200004'), 'MB2025072200004', '赵六', 1, '房费折扣', 1, 'BK202606010001', '{"discountRate":90,"originalPrice":598,"discountPrice":538.2}', 598.00, 59.80, 538.20, '金卡会员9折优惠', 1, '超级管理员', '2026-06-01 10:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025072200004'), 'MB2025072200004', '赵六', 5, '积分发放', 2, 'CI202606020001', '{"basePoints":538,"rate":1.5,"earnedPoints":807}', 0, 0, 0, '消费赠送积分', 1, '超级管理员', '2026-06-03 12:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025113000006'), 'MB2025113000006', '孙八', 2, '免费升级房型', 2, 'CI202606100001', '{"originalRoomType":"豪华大床房","newRoomType":"行政套房"}', 0, 200.00, 0, '钻石会员免费升级房型', 1, '超级管理员', '2026-06-10 14:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025113000006'), 'MB2025113000006', '孙八', 3, '押金减免', 2, 'CI202606100001', '{"standardDeposit":500,"reductionRate":100,"actualDeposit":0}', 500.00, 500.00, 0, '钻石会员免押金', 1, '超级管理员', '2026-06-10 14:05:00'),
((SELECT id FROM member WHERE member_no = 'MB2025113000006'), 'MB2025113000006', '孙八', 4, '延迟退房', 2, 'CI202606100001', '{"standardCheckout":"12:00","actualCheckout":"16:00","delayHours":4}', 0, 0, 0, '钻石会员延迟退房至16:00', 1, '超级管理员', '2026-06-12 15:30:00'),
((SELECT id FROM member WHERE member_no = 'MB2025032000002'), 'MB2025032000002', '李四', 1, '房费折扣', 1, 'BK202606150001', '{"discountRate":95,"originalPrice":398,"discountPrice":378.1}', 398.00, 19.90, 378.10, '银卡会员95折优惠', 1, '超级管理员', '2026-06-15 09:00:00'),
((SELECT id FROM member WHERE member_no = 'MB2025032000002'), 'MB2025032000002', '李四', 3, '押金减免', 2, 'CI202606160001', '{"standardDeposit":300,"reductionRate":50,"actualDeposit":150}', 300.00, 150.00, 150.00, '银卡会员减免50%押金', 1, '超级管理员', '2026-06-16 14:00:00');
