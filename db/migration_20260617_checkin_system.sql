SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
-- =============================================
-- 入住退房管理系统数据库迁移
-- 功能：入住登记、证件采集、押金收取、换房续住、费用结算、退房管理
-- =============================================

USE permission_system;

-- =============================================
-- 1. 入住单主表
-- =============================================
CREATE TABLE IF NOT EXISTS check_in (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '入住单ID',
    check_in_no VARCHAR(30) NOT NULL UNIQUE COMMENT '入住单号：yyyyMMddHHmmss+4位随机数',
    booking_id BIGINT COMMENT '关联预订单ID',
    booking_no VARCHAR(30) COMMENT '关联预订单号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(50) NOT NULL COMMENT '客户姓名',
    customer_phone VARCHAR(20) NOT NULL COMMENT '客户手机号',
    customer_type TINYINT DEFAULT 1 COMMENT '客户类型：1-散客，2-会员，3-协议客户，4-团队',
    member_level TINYINT DEFAULT 0 COMMENT '会员等级：0-非会员，1-普通会员，2-银卡，3-金卡，4-钻石',
    room_type_id BIGINT NOT NULL COMMENT '房型ID',
    room_type_name VARCHAR(50) NOT NULL COMMENT '房型名称',
    room_id BIGINT NOT NULL COMMENT '房间ID',
    room_number VARCHAR(20) NOT NULL COMMENT '房间号',
    check_in_date DATE NOT NULL COMMENT '入住日期',
    check_out_date DATE NOT NULL COMMENT '预计退房日期',
    actual_check_in_time DATETIME NOT NULL COMMENT '实际入住时间',
    actual_check_out_time DATETIME COMMENT '实际退房时间',
    days INT NOT NULL COMMENT '入住天数',
    stayed_days INT DEFAULT 0 COMMENT '已住天数',
    room_price DECIMAL(10,2) NOT NULL COMMENT '房费单价',
    room_total DECIMAL(10,2) NOT NULL COMMENT '房费小计',
    extra_bed_count INT DEFAULT 0 COMMENT '加床数量',
    extra_bed_price DECIMAL(10,2) DEFAULT 0 COMMENT '加床单价',
    extra_bed_total DECIMAL(10,2) DEFAULT 0 COMMENT '加床小计',
    other_fee DECIMAL(10,2) DEFAULT 0 COMMENT '其他消费',
    discount DECIMAL(10,2) DEFAULT 0 COMMENT '优惠折扣',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '应付总额',
    paid_amount DECIMAL(10,2) DEFAULT 0 COMMENT '已付金额',
    payable_amount DECIMAL(10,2) DEFAULT 0 COMMENT '待结算金额',
    deposit_amount DECIMAL(10,2) DEFAULT 0 COMMENT '押金金额',
    deposit_method TINYINT DEFAULT 1 COMMENT '押金方式：1-现金押金，2-信用卡预授权，3-免押金',
    deposit_voucher_no VARCHAR(50) COMMENT '押金凭证号/预授权号',
    deposit_authorized_by BIGINT COMMENT '免押金授权人ID',
    deposit_authorized_name VARCHAR(50) COMMENT '免押金授权人姓名',
    key_card_count INT DEFAULT 0 COMMENT '发放房卡数量',
    key_card_returned INT DEFAULT 0 COMMENT '已回收房卡数量',
    guest_count INT DEFAULT 1 COMMENT '入住人数',
    special_requirements VARCHAR(1000) COMMENT '特殊要求',
    booking_source TINYINT DEFAULT 1 COMMENT '预订来源：1-前台，2-官网，3-第三方平台，4-协议单位，5-旅行社，6-其他',
    status TINYINT DEFAULT 1 NOT NULL COMMENT '入住状态：1-在住，2-已退房，3-超期未退',
    is_overdue TINYINT DEFAULT 0 COMMENT '是否超期：0-否，1-是',
    operator_id BIGINT NOT NULL COMMENT '办理人ID',
    operator_name VARCHAR(50) NOT NULL COMMENT '办理人姓名',
    check_out_operator_id BIGINT COMMENT '退房办理人ID',
    check_out_operator_name VARCHAR(50) COMMENT '退房办理人姓名',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_check_in_no (check_in_no),
    INDEX idx_booking_id (booking_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_room_id (room_id),
    INDEX idx_status (status),
    INDEX idx_check_in_date (check_in_date),
    INDEX idx_check_out_date (check_out_date),
    INDEX idx_create_time (create_time),
    INDEX idx_operator_id (operator_id),
    INDEX idx_is_overdue (is_overdue)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入住单主表';

-- =============================================
-- 2. 入住人信息表
-- =============================================
CREATE TABLE IF NOT EXISTS check_in_guest (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '入住人ID',
    check_in_id BIGINT NOT NULL COMMENT '入住单ID',
    check_in_no VARCHAR(30) NOT NULL COMMENT '入住单号',
    is_main TINYINT DEFAULT 0 COMMENT '是否主入住人：0-否，1-是',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender TINYINT COMMENT '性别：1-男，2-女',
    id_type TINYINT DEFAULT 1 COMMENT '证件类型：1-身份证，2-护照，3-港澳通行证，4-台胞证，5-军官证，6-其他',
    id_number VARCHAR(50) NOT NULL COMMENT '证件号码',
    id_expiry_date DATE COMMENT '证件有效期',
    id_photo_front VARCHAR(500) COMMENT '证件照正面',
    id_photo_back VARCHAR(500) COMMENT '证件照反面',
    phone VARCHAR(20) COMMENT '联系电话',
    nationality VARCHAR(50) COMMENT '国籍',
    address VARCHAR(500) COMMENT '住址',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_check_in_id (check_in_id),
    INDEX idx_check_in_no (check_in_no),
    INDEX idx_id_number (id_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入住人信息表';

-- =============================================
-- 3. 房卡记录表
-- =============================================
CREATE TABLE IF NOT EXISTS key_card_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    check_in_id BIGINT NOT NULL COMMENT '入住单ID',
    check_in_no VARCHAR(30) NOT NULL COMMENT '入住单号',
    room_id BIGINT NOT NULL COMMENT '房间ID',
    room_number VARCHAR(20) NOT NULL COMMENT '房间号',
    card_no VARCHAR(50) NOT NULL COMMENT '房卡号',
    card_type TINYINT DEFAULT 1 COMMENT '房卡类型：1-普通房卡，2-电子房卡，3-临时卡',
    issue_time DATETIME NOT NULL COMMENT '发放时间',
    expire_time DATETIME COMMENT '有效期至',
    return_time DATETIME COMMENT '归还时间',
    status TINYINT DEFAULT 1 COMMENT '状态：1-使用中，2-已归还，3-已挂失，4-已注销',
    operator_id BIGINT COMMENT '发放人ID',
    operator_name VARCHAR(50) COMMENT '发放人姓名',
    return_operator_id BIGINT COMMENT '回收人ID',
    return_operator_name VARCHAR(50) COMMENT '回收人姓名',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_check_in_id (check_in_id),
    INDEX idx_check_in_no (check_in_no),
    INDEX idx_card_no (card_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房卡记录表';

-- =============================================
-- 4. 换房记录表
-- =============================================
CREATE TABLE IF NOT EXISTS room_change_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    change_no VARCHAR(30) NOT NULL UNIQUE COMMENT '换房单号',
    check_in_id BIGINT NOT NULL COMMENT '入住单ID',
    check_in_no VARCHAR(30) NOT NULL COMMENT '入住单号',
    old_room_id BIGINT NOT NULL COMMENT '原房间ID',
    old_room_number VARCHAR(20) NOT NULL COMMENT '原房间号',
    old_room_type_id BIGINT NOT NULL COMMENT '原房型ID',
    old_room_type_name VARCHAR(50) NOT NULL COMMENT '原房型名称',
    old_room_price DECIMAL(10,2) NOT NULL COMMENT '原房价',
    new_room_id BIGINT NOT NULL COMMENT '新房间ID',
    new_room_number VARCHAR(20) NOT NULL COMMENT '新房间号',
    new_room_type_id BIGINT NOT NULL COMMENT '新房型ID',
    new_room_type_name VARCHAR(50) NOT NULL COMMENT '新房型名称',
    new_room_price DECIMAL(10,2) NOT NULL COMMENT '新房价',
    price_diff DECIMAL(10,2) DEFAULT 0 COMMENT '差价（正为补，负为退）',
    change_reason TINYINT COMMENT '换房原因：1-客户要求，2-房间问题，3-升级房型，4-降级房型，5-其他',
    change_detail VARCHAR(1000) COMMENT '详细说明',
    change_time DATETIME NOT NULL COMMENT '换房时间',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(50) NOT NULL COMMENT '操作人姓名',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_change_no (change_no),
    INDEX idx_check_in_id (check_in_id),
    INDEX idx_check_in_no (check_in_no),
    INDEX idx_old_room_id (old_room_id),
    INDEX idx_new_room_id (new_room_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='换房记录表';

-- =============================================
-- 5. 续住记录表
-- =============================================
CREATE TABLE IF NOT EXISTS extend_stay_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    extend_no VARCHAR(30) NOT NULL UNIQUE COMMENT '续住单号',
    check_in_id BIGINT NOT NULL COMMENT '入住单ID',
    check_in_no VARCHAR(30) NOT NULL COMMENT '入住单号',
    room_id BIGINT NOT NULL COMMENT '房间ID',
    room_number VARCHAR(20) NOT NULL COMMENT '房间号',
    original_check_out_date DATE NOT NULL COMMENT '原退房日期',
    new_check_out_date DATE NOT NULL COMMENT '新退房日期',
    extend_days INT NOT NULL COMMENT '续住天数',
    room_price DECIMAL(10,2) NOT NULL COMMENT '续住房价',
    extend_amount DECIMAL(10,2) NOT NULL COMMENT '续住费用',
    reason VARCHAR(500) COMMENT '续住原因',
    extend_time DATETIME NOT NULL COMMENT '续住时间',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(50) NOT NULL COMMENT '操作人姓名',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_extend_no (extend_no),
    INDEX idx_check_in_id (check_in_id),
    INDEX idx_check_in_no (check_in_no),
    INDEX idx_room_id (room_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='续住记录表';

-- =============================================
-- 6. 退房记录表
-- =============================================
CREATE TABLE IF NOT EXISTS check_out_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    check_out_no VARCHAR(30) NOT NULL UNIQUE COMMENT '退房单号',
    check_in_id BIGINT NOT NULL COMMENT '入住单ID',
    check_in_no VARCHAR(30) NOT NULL COMMENT '入住单号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(50) NOT NULL COMMENT '客户姓名',
    room_id BIGINT NOT NULL COMMENT '房间ID',
    room_number VARCHAR(20) NOT NULL COMMENT '房间号',
    check_in_date DATE NOT NULL COMMENT '入住日期',
    check_out_date DATE NOT NULL COMMENT '实际退房日期',
    actual_check_out_time DATETIME NOT NULL COMMENT '实际退房时间',
    stayed_days INT NOT NULL COMMENT '实际住宿天数',
    room_total DECIMAL(10,2) DEFAULT 0 COMMENT '房费总额',
    extra_bed_total DECIMAL(10,2) DEFAULT 0 COMMENT '加床费',
    other_fee DECIMAL(10,2) DEFAULT 0 COMMENT '其他消费',
    damage_compensation DECIMAL(10,2) DEFAULT 0 COMMENT '损坏赔偿',
    discount DECIMAL(10,2) DEFAULT 0 COMMENT '优惠折扣',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '应付总额',
    paid_amount DECIMAL(10,2) DEFAULT 0 COMMENT '已付金额',
    deposit_amount DECIMAL(10,2) DEFAULT 0 COMMENT '押金金额',
    deposit_deducted DECIMAL(10,2) DEFAULT 0 COMMENT '押金抵扣金额',
    deposit_refund DECIMAL(10,2) DEFAULT 0 COMMENT '押金退还金额',
    additional_payment DECIMAL(10,2) DEFAULT 0 COMMENT '补收金额',
    refund_amount DECIMAL(10,2) DEFAULT 0 COMMENT '退款金额',
    payable_amount DECIMAL(10,2) DEFAULT 0 COMMENT '实际结算金额（正为收，负为退）',
    payment_method TINYINT COMMENT '支付方式：1-现金，2-刷卡，3-移动支付，4-押金抵扣，5-协议挂账',
    payment_voucher_no VARCHAR(50) COMMENT '支付凭证号',
    deposit_method TINYINT COMMENT '押金处理方式：1-现金退还，2-预授权解冻，3-抵扣费用，4-免押金无需处理',
    key_card_returned INT DEFAULT 0 COMMENT '回收房卡数',
    key_card_lost INT DEFAULT 0 COMMENT '遗失房卡数',
    room_checked TINYINT DEFAULT 0 COMMENT '是否查房：0-否，1-是',
    room_check_result TINYINT COMMENT '查房结果：1-正常，2-有物品损坏',
    damage_items VARCHAR(1000) COMMENT '损坏物品（JSON格式）',
    damage_description VARCHAR(1000) COMMENT '损坏描述',
    damage_photos VARCHAR(2000) COMMENT '损坏照片（逗号分隔）',
    status TINYINT DEFAULT 1 COMMENT '状态：1-已完成，2-待确认',
    operator_id BIGINT NOT NULL COMMENT '办理人ID',
    operator_name VARCHAR(50) NOT NULL COMMENT '办理人姓名',
    remark VARCHAR(1000) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_check_out_no (check_out_no),
    INDEX idx_check_in_id (check_in_id),
    INDEX idx_check_in_no (check_in_no),
    INDEX idx_customer_id (customer_id),
    INDEX idx_room_id (room_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退房记录表';

-- =============================================
-- 7. 入住操作日志表
-- =============================================
CREATE TABLE IF NOT EXISTS check_in_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    check_in_id BIGINT NOT NULL COMMENT '入住单ID',
    check_in_no VARCHAR(30) NOT NULL COMMENT '入住单号',
    operation_type TINYINT NOT NULL COMMENT '操作类型：1-办理入住，2-信息修改，3-换房，4-续住，5-添加消费，6-押金变更，7-房卡发放，8-房卡回收，9-办理退房，10-取消退房',
    operation_desc VARCHAR(200) NOT NULL COMMENT '操作描述',
    detail TEXT COMMENT '操作详情（JSON）',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    operator_role VARCHAR(50) COMMENT '操作人角色',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_check_in_id (check_in_id),
    INDEX idx_check_in_no (check_in_no),
    INDEX idx_operation_type (operation_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入住操作日志表';

-- =============================================
-- 8. 消费记录表
-- =============================================
CREATE TABLE IF NOT EXISTS consumption_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    consumption_no VARCHAR(30) NOT NULL UNIQUE COMMENT '消费单号',
    check_in_id BIGINT NOT NULL COMMENT '入住单ID',
    check_in_no VARCHAR(30) NOT NULL COMMENT '入住单号',
    room_id BIGINT COMMENT '房间ID',
    room_number VARCHAR(20) COMMENT '房间号',
    customer_id BIGINT COMMENT '客户ID',
    customer_name VARCHAR(50) COMMENT '客户姓名',
    consumption_type TINYINT NOT NULL COMMENT '消费类型：1-房费，2-餐饮，3-迷你吧，4-洗衣，5-通讯，6-接送，7-其他',
    item_name VARCHAR(200) NOT NULL COMMENT '消费项目名称',
    quantity DECIMAL(10,2) DEFAULT 1 COMMENT '数量',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '单价',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '金额',
    consumption_time DATETIME NOT NULL COMMENT '消费时间',
    billing_method TINYINT DEFAULT 1 COMMENT '记账方式：1-挂房账，2-现付',
    is_settled TINYINT DEFAULT 0 COMMENT '是否已结算：0-否，1-是',
    settle_time DATETIME COMMENT '结算时间',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_consumption_no (consumption_no),
    INDEX idx_check_in_id (check_in_id),
    INDEX idx_check_in_no (check_in_no),
    INDEX idx_customer_id (customer_id),
    INDEX idx_consumption_type (consumption_type),
    INDEX idx_is_settled (is_settled),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费记录表';

-- =============================================
-- 9. 入住管理菜单 (ID range: 500-599)
-- =============================================
INSERT IGNORE INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(500, '入住管理', 0, 4, '/checkin', NULL, NULL, 0, 1, 1, 'Key'),
(501, '入住单管理', 500, 1, '/checkin/list', 'checkin/CheckInList', 'checkin:list', 1, 1, 1, 'Document'),
(502, '办理入住', 500, 2, '/checkin/create', 'checkin/CheckInCreate', 'checkin:create', 1, 1, 1, 'Plus'),
(503, '散客入住', 500, 3, '/checkin/walkin', 'checkin/WalkInCreate', 'checkin:walkin', 1, 1, 1, 'User'),
(504, '办理退房', 500, 4, '/checkin/checkout', 'checkin/CheckOut', 'checkin:checkout', 1, 1, 1, 'SwitchButton'),
(505, '入住统计', 500, 5, '/checkin/statistics', 'checkin/CheckInStatistics', 'checkin:statistics:list', 1, 1, 1, 'DataLine');

-- 入住单管理按钮权限
INSERT IGNORE INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(511, '入住单查询', 501, 1, '', NULL, 'checkin:query', 2, 1, 1, NULL),
(512, '入住单创建', 501, 2, '', NULL, 'checkin:create', 2, 1, 1, NULL),
(513, '入住单编辑', 501, 3, '', NULL, 'checkin:edit', 2, 1, 1, NULL),
(514, '办理退房', 501, 4, '', NULL, 'checkin:checkout', 2, 1, 1, NULL),
(515, '办理换房', 501, 5, '', NULL, 'checkin:changeRoom', 2, 1, 1, NULL),
(516, '办理续住', 501, 6, '', NULL, 'checkin:extend', 2, 1, 1, NULL),
(517, '添加消费', 501, 7, '', NULL, 'checkin:addConsumption', 2, 1, 1, NULL),
(518, '押金管理', 501, 8, '', NULL, 'checkin:deposit:manage', 2, 1, 1, NULL),
(519, '免押金授权', 501, 9, '', NULL, 'checkin:deposit:free', 2, 1, 1, NULL),
(520, '入住单导出', 501, 10, '', NULL, 'checkin:export', 2, 1, 1, NULL),
(521, '损坏赔偿处理', 501, 11, '', NULL, 'checkin:damage:handle', 2, 1, 1, NULL);

-- 入住统计按钮权限
INSERT IGNORE INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(531, '统计查询', 505, 1, '', NULL, 'checkin:statistics:query', 2, 1, 1, NULL),
(532, '统计导出', 505, 2, '', NULL, 'checkin:statistics:export', 2, 1, 1, NULL);

-- =============================================
-- 10. 分配角色权限 - 超级管理员
-- =============================================
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 500), (1, 501), (1, 502), (1, 503), (1, 504), (1, 505),
(1, 511), (1, 512), (1, 513), (1, 514), (1, 515), (1, 516), (1, 517), (1, 518), (1, 519), (1, 520), (1, 521),
(1, 531), (1, 532);

-- =============================================
-- 11. 分配角色权限 - 酒店管理员(hotel_admin)
-- =============================================
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(3, 500), (3, 501), (3, 502), (3, 503), (3, 504), (3, 505),
(3, 511), (3, 512), (3, 513), (3, 514), (3, 515), (3, 516), (3, 517), (3, 518), (3, 519), (3, 520), (3, 521),
(3, 531), (3, 532);

-- =============================================
-- 12. 分配角色权限 - 前厅部经理(frontdesk_manager)
-- =============================================
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(4, 500), (4, 501), (4, 502), (4, 503), (4, 504), (4, 505),
(4, 511), (4, 512), (4, 513), (4, 514), (4, 515), (4, 516), (4, 517), (4, 518), (4, 519), (4, 521),
(4, 531);

-- =============================================
-- 13. 分配角色权限 - 普通前台(receptionist)
-- =============================================
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(6, 500), (6, 501), (6, 502), (6, 503), (6, 504),
(6, 511), (6, 512), (6, 513), (6, 514), (6, 515), (6, 516), (6, 517), (6, 518);

-- =============================================
-- 14. 分配角色权限 - 客房部经理(housekeeping_manager)
-- =============================================
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(5, 500), (5, 501), (5, 505),
(5, 511),
(5, 531);

-- =============================================
-- 15. 分配角色权限 - 财务人员(finance_staff)
-- =============================================
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(7, 500), (7, 501), (7, 505),
(7, 511), (7, 520),
(7, 531), (7, 532);
