SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE permission_system;

-- =============================================
-- 客户表
-- =============================================
CREATE TABLE IF NOT EXISTS customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '客户ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    birth_date DATE COMMENT '出生日期',
    nationality VARCHAR(50) DEFAULT '中国' COMMENT '国籍',
    avatar VARCHAR(500) COMMENT '客户头像URL',

    id_type TINYINT DEFAULT 1 COMMENT '证件类型：1-身份证，2-护照，3-港澳通行证，4-台胞证，5-驾驶证',
    id_number VARCHAR(50) COMMENT '证件号码',
    id_expiry_date DATE COMMENT '证件有效期',
    id_photo_front VARCHAR(500) COMMENT '证件正面照片URL',
    id_photo_back VARCHAR(500) COMMENT '证件反面照片URL',

    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    backup_phone VARCHAR(20) COMMENT '备用手机号',
    email VARCHAR(100) COMMENT '邮箱地址',
    wechat VARCHAR(50) COMMENT '微信号',
    emergency_contact_name VARCHAR(50) COMMENT '紧急联系人姓名',
    emergency_contact_relation VARCHAR(20) COMMENT '紧急联系人关系',
    emergency_contact_phone VARCHAR(20) COMMENT '紧急联系人电话',

    customer_type TINYINT DEFAULT 1 COMMENT '客户类型：1-散客，2-企业客户，3-协议客户，4-VIP客户',
    customer_source TINYINT DEFAULT 1 COMMENT '客户来源：1-官网，2-OTA平台，3-企业协议，4-电话预订，5-前台登记，6-老客户推荐',
    referrer_id BIGINT COMMENT '推荐人客户ID',
    importance TINYINT DEFAULT 1 COMMENT '重要程度：1-普通，2-重要，3-VIP',
    status TINYINT DEFAULT 1 COMMENT '客户状态：1-正常，2-冻结',

    first_order_time DATETIME COMMENT '首次消费时间（预留）',
    total_orders INT DEFAULT 0 COMMENT '累计入住次数（预留）',
    total_spent DECIMAL(12,2) DEFAULT 0 COMMENT '累计消费金额（预留）',
    last_order_time DATETIME COMMENT '最近入住时间（预留）',

    freeze_reason VARCHAR(500) COMMENT '冻结原因',
    freeze_time DATETIME COMMENT '冻结时间',
    freeze_operator_id BIGINT COMMENT '冻结操作人ID',
    freeze_operator_name VARCHAR(50) COMMENT '冻结操作人姓名',
    unfreeze_reason VARCHAR(500) COMMENT '解冻原因',
    unfreeze_time DATETIME COMMENT '解冻时间',
    unfreeze_operator_id BIGINT COMMENT '解冻操作人ID',
    unfreeze_operator_name VARCHAR(50) COMMENT '解冻操作人姓名',

    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',

    UNIQUE INDEX idx_phone (phone),
    UNIQUE INDEX idx_id_number (id_type, id_number),
    INDEX idx_customer_type (customer_type),
    INDEX idx_customer_source (customer_source),
    INDEX idx_status (status),
    INDEX idx_importance (importance),
    INDEX idx_create_time (create_time),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户表';

-- =============================================
-- 客户常用地址表
-- =============================================
CREATE TABLE IF NOT EXISTS customer_address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '地址ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    address_type TINYINT DEFAULT 1 COMMENT '地址类型：1-家庭地址，2-公司地址，3-发票邮寄地址',
    province VARCHAR(50) COMMENT '省',
    city VARCHAR(50) COMMENT '市',
    district VARCHAR(50) COMMENT '区',
    detail VARCHAR(500) NOT NULL COMMENT '详细地址',
    postal_code VARCHAR(10) COMMENT '邮编',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认地址：0-否，1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_customer_id (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户常用地址表';

-- =============================================
-- 客户操作记录表
-- =============================================
CREATE TABLE IF NOT EXISTS customer_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(50) COMMENT '客户姓名',
    operation_type TINYINT NOT NULL COMMENT '操作类型：1-创建客户，2-修改客户信息，3-冻结客户，4-解冻客户',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_customer_id (customer_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户操作记录表';

-- =============================================
-- 新增角色：客服主管
-- =============================================
INSERT INTO sys_role (id, role_name, role_key, order_num, status, remark) VALUES
(11, '客服主管', 'service_manager', 11, 1, '客服主管，可查看和修改客户信息，可冻结/解冻客户');

INSERT INTO sys_user (id, username, password, nickname, email, phone, status) VALUES
(12, 'service_manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtE/ETXmB5nNiHxqHnHfgVd5GK6', '客服主管', 'service@example.com', '13800138011', 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES
(12, 11);

INSERT INTO sys_data_permission (role_id, scope_type, custom_dept_ids) VALUES
(11, 1, NULL);

-- =============================================
-- 客户管理菜单 (ID range: 300-399)
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(300, '客户管理', 0, 2, '/customer', NULL, NULL, 0, 1, 1, 'UserFilled'),
(301, '客户列表', 300, 1, '/customer/list', 'customer/CustomerList', 'customer:list', 1, 1, 1, 'User'),
(302, '新增客户', 300, 2, '/customer/create', 'customer/CustomerCreate', 'customer:add', 1, 1, 1, 'Plus'),
(303, '客户详情', 300, 3, '/customer/detail/:id', 'customer/CustomerDetail', 'customer:query', 1, 1, 1, 'View'),
(304, '客户编辑', 300, 4, '/customer/edit/:id', 'customer/CustomerEdit', 'customer:edit', 1, 1, 1, 'Edit');

-- 客户管理按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(311, '客户查询', 301, 1, '', NULL, 'customer:query', 2, 1, 1, NULL),
(312, '客户新增', 301, 2, '', NULL, 'customer:add', 2, 1, 1, NULL),
(313, '客户修改', 301, 3, '', NULL, 'customer:edit', 2, 1, 1, NULL),
(314, '客户删除', 301, 4, '', NULL, 'customer:delete', 2, 1, 1, NULL),
(315, '客户冻结', 301, 5, '', NULL, 'customer:freeze', 2, 1, 1, NULL),
(316, '客户解冻', 301, 6, '', NULL, 'customer:unfreeze', 2, 1, 1, NULL),
(317, '客户消费查看', 301, 7, '', NULL, 'customer:finance:view', 2, 1, 1, NULL),
(318, '客户地址管理', 301, 8, '', NULL, 'customer:address:manage', 2, 1, 1, NULL);

-- =============================================
-- 超级管理员：客户管理所有权限
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 300), (1, 301), (1, 302), (1, 303), (1, 304),
(1, 311), (1, 312), (1, 313), (1, 314), (1, 315), (1, 316), (1, 317), (1, 318);

-- =============================================
-- 酒店管理员(hotel_admin)：客户管理所有权限
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 300), (3, 301), (3, 302), (3, 303), (3, 304),
(3, 311), (3, 312), (3, 313), (3, 314), (3, 315), (3, 316), (3, 317), (3, 318);

-- =============================================
-- 前台员工(receptionist)：
-- 可以新增客户、查看基础信息、修改联系方式和地址
-- 看不到累计消费金额、不能冻结/删除客户
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(6, 300), (6, 301), (6, 302),
(6, 311), (6, 312), (6, 313), (6, 318);

-- =============================================
-- 客服主管(service_manager)：
-- 可以查看所有客户信息、修改所有字段、冻结/解冻客户、查看完整统计数据
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(11, 300), (11, 301), (11, 302), (11, 303), (11, 304),
(11, 311), (11, 312), (11, 313), (11, 315), (11, 316), (11, 317), (11, 318);

-- =============================================
-- 财务人员(finance_staff)：
-- 可以查看客户消费统计数据、查看客户基础信息
-- 不能修改客户信息、不能冻结客户
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(7, 300), (7, 301),
(7, 311), (7, 317);

-- =============================================
-- 前厅部经理(frontdesk_manager)：
-- 可查看客户信息、新增客户、修改客户信息、冻结/解冻客户
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 300), (4, 301), (4, 302), (4, 303), (4, 304),
(4, 311), (4, 312), (4, 313), (4, 315), (4, 316), (4, 317), (4, 318);
