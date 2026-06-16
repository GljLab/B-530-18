SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
-- =============================================
-- 预订管理系统数据库迁移
-- 功能：房源查询、预订管理、状态流转、预订变更、取消退款、统计报表
-- =============================================

USE permission_system;

-- =============================================
-- 1. 预订单主表
-- =============================================
CREATE TABLE IF NOT EXISTS booking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '预订单ID',
    booking_no VARCHAR(30) NOT NULL UNIQUE COMMENT '预订单号：yyyyMMddHHmmss+4位随机数',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(50) NOT NULL COMMENT '客户姓名',
    customer_phone VARCHAR(20) NOT NULL COMMENT '客户手机号',
    room_type_id BIGINT NOT NULL COMMENT '房型ID',
    room_type_name VARCHAR(50) NOT NULL COMMENT '房型名称',
    room_id BIGINT NOT NULL COMMENT '房间ID',
    room_number VARCHAR(20) NOT NULL COMMENT '房间号',
    check_in_date DATE NOT NULL COMMENT '入住日期',
    check_out_date DATE NOT NULL COMMENT '退房日期',
    days INT NOT NULL COMMENT '入住天数',
    check_in_time DATETIME COMMENT '实际入住时间',
    check_out_time DATETIME COMMENT '实际退房时间',
    room_price DECIMAL(10,2) NOT NULL COMMENT '房费单价',
    room_total DECIMAL(10,2) NOT NULL COMMENT '房费小计',
    extra_bed_count INT DEFAULT 0 COMMENT '加床数量',
    extra_bed_price DECIMAL(10,2) DEFAULT 0 COMMENT '加床单价',
    extra_bed_total DECIMAL(10,2) DEFAULT 0 COMMENT '加床小计',
    other_fee DECIMAL(10,2) DEFAULT 0 COMMENT '其他费用',
    discount DECIMAL(10,2) DEFAULT 0 COMMENT '优惠折扣',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '应付总额',
    paid_amount DECIMAL(10,2) DEFAULT 0 COMMENT '已付金额',
    guest_count INT DEFAULT 1 COMMENT '入住人数',
    guest_names VARCHAR(500) COMMENT '入住人姓名（逗号分隔）',
    guest_phone VARCHAR(20) COMMENT '入住人联系电话',
    special_requirements VARCHAR(1000) COMMENT '特殊要求备注',
    expected_arrival_time DATETIME COMMENT '预计到店时间',
    guarantee_type TINYINT NOT NULL COMMENT '担保方式：1-到店支付，2-信用卡担保，3-预付全款',
    booking_source TINYINT NOT NULL COMMENT '预订来源：1-前台，2-官网，3-第三方平台，4-协议单位，5-旅行社，6-其他',
    source_remark VARCHAR(200) COMMENT '来源备注',
    status TINYINT DEFAULT 1 NOT NULL COMMENT '预订状态：1-待确认，2-已确认，3-已支付，4-已入住，5-已完成，6-已取消',
    cancel_reason VARCHAR(200) COMMENT '取消原因',
    cancel_detail VARCHAR(1000) COMMENT '取消详细说明',
    cancel_penalty DECIMAL(10,2) DEFAULT 0 COMMENT '取消违约金',
    cancel_time DATETIME COMMENT '取消时间',
    cancel_operator_id BIGINT COMMENT '取消操作人ID',
    cancel_operator_name VARCHAR(50) COMMENT '取消操作人姓名',
    create_user_id BIGINT NOT NULL COMMENT '创建人ID',
    create_user_name VARCHAR(50) NOT NULL COMMENT '创建人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_booking_no (booking_no),
    INDEX idx_customer_id (customer_id),
    INDEX idx_room_id (room_id),
    INDEX idx_status (status),
    INDEX idx_check_in_date (check_in_date),
    INDEX idx_check_out_date (check_out_date),
    INDEX idx_create_time (create_time),
    INDEX idx_booking_source (booking_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预订单主表';

-- =============================================
-- 2. 预订价格明细表（逐日价格）
-- =============================================
CREATE TABLE IF NOT EXISTS booking_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '明细ID',
    booking_id BIGINT NOT NULL COMMENT '预订单ID',
    booking_no VARCHAR(30) NOT NULL COMMENT '预订单号',
    stay_date DATE NOT NULL COMMENT '入住日期',
    day_type VARCHAR(10) NOT NULL COMMENT '日期类型：平日、周末、节假日',
    price DECIMAL(10,2) NOT NULL COMMENT '当日价格',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_booking_id (booking_id),
    INDEX idx_booking_no (booking_no),
    INDEX idx_stay_date (stay_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预订价格明细表';

-- =============================================
-- 3. 预订状态流转日志表
-- =============================================
CREATE TABLE IF NOT EXISTS booking_status_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    booking_id BIGINT NOT NULL COMMENT '预订单ID',
    booking_no VARCHAR(30) NOT NULL COMMENT '预订单号',
    old_status TINYINT COMMENT '原状态',
    new_status TINYINT NOT NULL COMMENT '新状态',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    remark VARCHAR(500) COMMENT '操作备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_booking_id (booking_id),
    INDEX idx_booking_no (booking_no),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预订状态流转日志表';

-- =============================================
-- 4. 预订变更日志表
-- =============================================
CREATE TABLE IF NOT EXISTS booking_change_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    booking_id BIGINT NOT NULL COMMENT '预订单ID',
    booking_no VARCHAR(30) NOT NULL COMMENT '预订单号',
    change_type TINYINT NOT NULL COMMENT '变更类型：1-信息修改，2-更换房间，3-价格变更',
    field_name VARCHAR(100) COMMENT '变更字段名',
    old_value TEXT COMMENT '原值',
    new_value TEXT COMMENT '新值',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    remark VARCHAR(500) COMMENT '变更备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    INDEX idx_booking_id (booking_id),
    INDEX idx_booking_no (booking_no),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预订变更日志表';

-- =============================================
-- 5. 退款记录表
-- =============================================
CREATE TABLE IF NOT EXISTS refund_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '退款记录ID',
    refund_no VARCHAR(30) NOT NULL UNIQUE COMMENT '退款单号',
    booking_id BIGINT NOT NULL COMMENT '预订单ID',
    booking_no VARCHAR(30) NOT NULL COMMENT '预订单号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(50) NOT NULL COMMENT '客户姓名',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总额',
    paid_amount DECIMAL(10,2) NOT NULL COMMENT '已付金额',
    refundable_amount DECIMAL(10,2) NOT NULL COMMENT '应退金额',
    deduction_amount DECIMAL(10,2) DEFAULT 0 COMMENT '扣除金额（违约金）',
    actual_refund_amount DECIMAL(10,2) NOT NULL COMMENT '实际退款金额',
    refund_reason VARCHAR(500) COMMENT '退款原因',
    refund_method TINYINT DEFAULT 1 COMMENT '退款方式：1-原路退回，2-人工退款',
    status TINYINT DEFAULT 1 NOT NULL COMMENT '退款状态：1-待退款，2-退款中，3-已退款，4-已拒绝',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    applicant_name VARCHAR(50) NOT NULL COMMENT '申请人姓名',
    approver_id BIGINT COMMENT '审批人ID',
    approver_name VARCHAR(50) COMMENT '审批人姓名',
    approve_remark VARCHAR(500) COMMENT '审批备注',
    approve_time DATETIME COMMENT '审批时间',
    refund_time DATETIME COMMENT '退款完成时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_refund_no (refund_no),
    INDEX idx_booking_id (booking_id),
    INDEX idx_booking_no (booking_no),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款记录表';

-- =============================================
-- 6. 新增角色：客服人员
-- =============================================
INSERT IGNORE INTO sys_role (id, role_name, role_key, order_num, status, remark) VALUES
(11, '客服人员', 'customer_service', 11, 1, '客服人员，可创建和查看预订单');

-- =============================================
-- 7. 新增测试用户：客服人员
-- =============================================
INSERT IGNORE INTO sys_user (id, username, password, nickname, email, phone, status) VALUES
(12, 'customer_service', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtE/ETXmB5nNiHxqHnHfgVd5GK6', '客服人员', 'service@example.com', '13800138011', 1);

INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES
(12, 11);

INSERT IGNORE INTO sys_data_permission (role_id, scope_type, custom_dept_ids) VALUES
(11, 1, NULL);

-- =============================================
-- 8. 预订管理菜单 (ID range: 400-499，避免与客户管理300-399冲突)
-- =============================================
INSERT IGNORE INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(400, '预订管理', 0, 3, '/booking', NULL, NULL, 0, 1, 1, 'Calendar'),
(401, '房源查询', 400, 1, '/booking/roomQuery', 'booking/RoomQuery', 'booking:roomQuery:list', 1, 1, 1, 'Search'),
(402, '创建预订', 400, 2, '/booking/create', 'booking/BookingCreate', 'booking:create', 1, 1, 1, 'Edit'),
(403, '预订单管理', 400, 3, '/booking/list', 'booking/BookingList', 'booking:list', 1, 1, 1, 'Document'),
(404, '预订统计', 400, 4, '/booking/statistics', 'booking/BookingStatistics', 'booking:statistics:list', 1, 1, 1, 'DataLine'),
(405, '预订日历', 400, 5, '/booking/calendar', 'booking/BookingCalendar', 'booking:calendar:view', 1, 1, 1, 'Date');

-- 房源查询按钮权限
INSERT IGNORE INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(411, '房源查询', 401, 1, '', NULL, 'booking:roomQuery:query', 2, 1, 1, NULL),
(412, '添加到预订清单', 401, 2, '', NULL, 'booking:roomQuery:add', 2, 1, 1, NULL);

-- 预订单管理按钮权限
INSERT IGNORE INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(421, '预订单查询', 403, 1, '', NULL, 'booking:query', 2, 1, 1, NULL),
(422, '预订单创建', 403, 2, '', NULL, 'booking:create', 2, 1, 1, NULL),
(423, '预订单修改', 403, 3, '', NULL, 'booking:edit', 2, 1, 1, NULL),
(424, '预订单确认', 403, 4, '', NULL, 'booking:confirm', 2, 1, 1, NULL),
(425, '预订单取消', 403, 5, '', NULL, 'booking:cancel', 2, 1, 1, NULL),
(426, '取消已支付订单', 403, 6, '', NULL, 'booking:cancelPaid', 2, 1, 1, NULL),
(427, '办理入住', 403, 7, '', NULL, 'booking:checkin', 2, 1, 1, NULL),
(428, '更换房间', 403, 8, '', NULL, 'booking:changeRoom', 2, 1, 1, NULL),
(429, '申请退款', 403, 9, '', NULL, 'booking:refund:apply', 2, 1, 1, NULL),
(430, '退款审批', 403, 10, '', NULL, 'booking:refund:approve', 2, 1, 1, NULL),
(431, '预订单导出', 403, 11, '', NULL, 'booking:export', 2, 1, 1, NULL),
(432, '批量确认', 403, 12, '', NULL, 'booking:batchConfirm', 2, 1, 1, NULL),
(433, '查看成本价', 403, 13, '', NULL, 'booking:cost:view', 2, 1, 1, NULL);

-- 预订统计按钮权限
INSERT IGNORE INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(441, '统计查询', 404, 1, '', NULL, 'booking:statistics:query', 2, 1, 1, NULL),
(442, '统计导出', 404, 2, '', NULL, 'booking:statistics:export', 2, 1, 1, NULL);

-- =============================================
-- 9. 分配角色权限 - 超级管理员
-- =============================================
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 400), (1, 401), (1, 402), (1, 403), (1, 404), (1, 405),
(1, 411), (1, 412),
(1, 421), (1, 422), (1, 423), (1, 424), (1, 425), (1, 426), (1, 427), (1, 428), (1, 429), (1, 430), (1, 431), (1, 432), (1, 433),
(1, 441), (1, 442);

-- =============================================
-- 10. 分配角色权限 - 酒店管理员(hotel_admin)
-- =============================================
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(3, 400), (3, 401), (3, 402), (3, 403), (3, 404), (3, 405),
(3, 411), (3, 412),
(3, 421), (3, 422), (3, 423), (3, 424), (3, 425), (3, 426), (3, 427), (3, 428), (3, 429), (3, 430), (3, 431), (3, 432), (3, 433),
(3, 441), (3, 442);

-- =============================================
-- 11. 分配角色权限 - 前厅部经理(frontdesk_manager)
-- =============================================
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(4, 400), (4, 401), (4, 402), (4, 403), (4, 404), (4, 405),
(4, 411), (4, 412),
(4, 421), (4, 422), (4, 423), (4, 424), (4, 425), (4, 426), (4, 427), (4, 428), (4, 429), (4, 431), (4, 432),
(4, 441), (4, 442);

-- =============================================
-- 12. 分配角色权限 - 客房部经理(housekeeping_manager)
-- =============================================
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(5, 400), (5, 403), (5, 404), (5, 405),
(5, 411),
(5, 421),
(5, 441);

-- =============================================
-- 13. 分配角色权限 - 普通前台(receptionist)
-- =============================================
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(6, 400), (6, 401), (6, 402), (6, 403),
(6, 411), (6, 412),
(6, 421), (6, 422), (6, 423), (6, 424), (6, 425), (6, 427), (6, 428);

-- =============================================
-- 14. 分配角色权限 - 财务人员(finance_staff)
-- =============================================
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(7, 400), (7, 403), (7, 404),
(7, 421), (7, 431), (7, 433),
(7, 441), (7, 442);

-- =============================================
-- 15. 分配角色权限 - 客服人员(customer_service)
-- =============================================
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(11, 400), (11, 401), (11, 402), (11, 403),
(11, 411), (11, 412),
(11, 421), (11, 422), (11, 423);

-- =============================================
-- 16. 初始化一些测试房型和房间数据（如果不存在）
-- =============================================
-- 检查是否有房型数据，如果没有则初始化
-- 注意：这些数据仅用于测试，实际使用时应通过系统管理功能添加
