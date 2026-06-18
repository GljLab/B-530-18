SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
-- =============================================
-- 财务结算管理系统数据库迁移
-- 功能：协议单位管理、挂账结算、退款审批、日结对账、交接班对账、收款汇总、应收账款监控
-- =============================================

USE permission_system;

-- =============================================
-- 1. 协议单位表
-- =============================================
CREATE TABLE IF NOT EXISTS agreement_unit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '协议单位ID',
    unit_name VARCHAR(100) NOT NULL COMMENT '单位名称',
    credit_code VARCHAR(50) COMMENT '统一社会信用代码',
    address VARCHAR(1000) COMMENT '单位地址',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_person VARCHAR(50) COMMENT '联系人姓名',
    contact_position VARCHAR(50) COMMENT '联系人职位',
    contact_mobile VARCHAR(20) COMMENT '联系人手机号',
    contact_email VARCHAR(100) COMMENT '联系人邮箱',
    cooperation_type TINYINT DEFAULT 1 COMMENT '合作类型：1-长期协议，2-临时协议',
    sign_date DATE COMMENT '签约日期',
    valid_until DATE COMMENT '协议有效期',
    settlement_cycle TINYINT DEFAULT 1 COMMENT '结算周期：1-月结，2-季度结，3-半年结',
    credit_limit DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '信用额度',
    current_debt DECIMAL(12,2) DEFAULT 0 COMMENT '当前欠款',
    discount_rate DECIMAL(3,2) DEFAULT 1.00 COMMENT '协议折扣(如0.9表示9折)',
    agreement_file VARCHAR(1000) COMMENT '协议附件路径',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    remark VARCHAR(1000) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_unit_name (unit_name),
    INDEX idx_credit_code (credit_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='协议单位表';

-- =============================================
-- 2. 挂账账单表
-- =============================================
CREATE TABLE IF NOT EXISTS credit_bill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '账单ID',
    bill_no VARCHAR(50) NOT NULL UNIQUE COMMENT '账单号',
    agreement_unit_id BIGINT NOT NULL COMMENT '协议单位ID',
    agreement_unit_name VARCHAR(100) COMMENT '协议单位名称',
    check_in_id BIGINT COMMENT '入住单ID',
    check_in_no VARCHAR(50) COMMENT '入住单号',
    customer_name VARCHAR(50) COMMENT '客户姓名',
    room_number VARCHAR(20) COMMENT '房号',
    check_in_date DATE COMMENT '入住日期',
    check_out_date DATE COMMENT '退房日期',
    room_fee DECIMAL(12,2) DEFAULT 0 COMMENT '房费',
    extra_fee DECIMAL(12,2) DEFAULT 0 COMMENT '额外费用',
    discount_amount DECIMAL(12,2) DEFAULT 0 COMMENT '折扣金额',
    total_amount DECIMAL(12,2) DEFAULT 0 COMMENT '总金额',
    bill_time DATETIME COMMENT '挂账时间',
    settlement_id BIGINT COMMENT '结算单ID',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待结算，1-已结算',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_agreement_unit_id (agreement_unit_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='挂账账单表';

-- =============================================
-- 3. 挂账结算表
-- =============================================
CREATE TABLE IF NOT EXISTS credit_settlement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '结算单ID',
    settlement_no VARCHAR(50) NOT NULL UNIQUE COMMENT '结算单号',
    agreement_unit_id BIGINT NOT NULL COMMENT '协议单位ID',
    agreement_unit_name VARCHAR(100) COMMENT '协议单位名称',
    period_start DATE COMMENT '结算周期开始',
    period_end DATE COMMENT '结算周期结束',
    bill_count INT DEFAULT 0 COMMENT '账单数量',
    total_amount DECIMAL(12,2) DEFAULT 0 COMMENT '结算总额',
    settlement_method TINYINT COMMENT '结算方式：1-银行转账，2-现金，3-支票',
    settlement_date DATE COMMENT '结算日期',
    voucher_no VARCHAR(50) COMMENT '结算凭证号',
    invoice_no VARCHAR(50) COMMENT '发票号',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待确认，1-已完成',
    remark VARCHAR(1000) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_agreement_unit_id (agreement_unit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='挂账结算表';

-- =============================================
-- 4. 退款申请表
-- =============================================
CREATE TABLE IF NOT EXISTS refund_apply (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '退款申请ID',
    refund_no VARCHAR(50) NOT NULL UNIQUE COMMENT '退款单号',
    related_order_type TINYINT COMMENT '关联单类型：1-预订单，2-入住单',
    related_order_id BIGINT COMMENT '关联单ID',
    related_order_no VARCHAR(50) COMMENT '关联单号',
    customer_id BIGINT COMMENT '客户ID',
    customer_name VARCHAR(50) COMMENT '客户姓名',
    customer_phone VARCHAR(20) COMMENT '客户手机号',
    refund_type TINYINT COMMENT '退款类型：1-预订取消退款，2-多收费用退款，3-客诉补偿退款，4-房间质量问题退款，5-其他',
    refund_amount DECIMAL(12,2) NOT NULL COMMENT '退款金额',
    approved_amount DECIMAL(12,2) COMMENT '审批金额',
    refund_reason TEXT COMMENT '退款原因',
    proof_materials VARCHAR(1000) COMMENT '证明材料(JSON数组)',
    applicant_id BIGINT COMMENT '申请人ID',
    applicant_name VARCHAR(50) COMMENT '申请人姓名',
    apply_time DATETIME COMMENT '申请时间',
    approver_id BIGINT COMMENT '审批人ID',
    approver_name VARCHAR(50) COMMENT '审批人姓名',
    approve_time DATETIME COMMENT '审批时间',
    approve_remark VARCHAR(1000) COMMENT '审批意见',
    reject_reason VARCHAR(1000) COMMENT '拒绝原因',
    refund_method TINYINT COMMENT '退款方式：1-原路退回，2-现金退款，3-转账退款',
    refund_voucher_no VARCHAR(50) COMMENT '退款凭证号',
    refund_time DATETIME COMMENT '退款时间',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待审批，1-已通过，2-已拒绝，3-已退款',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_related_order (related_order_type, related_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款申请表';

-- =============================================
-- 5. 日结对账表
-- =============================================
CREATE TABLE IF NOT EXISTS daily_reconciliation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日结对账ID',
    reconcile_date DATE NOT NULL COMMENT '对账日期',
    cash_room DECIMAL(12,2) DEFAULT 0 COMMENT '房费现金',
    cash_deposit DECIMAL(12,2) DEFAULT 0 COMMENT '押金现金',
    cash_other DECIMAL(12,2) DEFAULT 0 COMMENT '其他现金',
    cash_total DECIMAL(12,2) DEFAULT 0 COMMENT '现金总额',
    card_room DECIMAL(12,2) DEFAULT 0 COMMENT '房费刷卡',
    card_deposit DECIMAL(12,2) DEFAULT 0 COMMENT '押金刷卡',
    card_other DECIMAL(12,2) DEFAULT 0 COMMENT '其他刷卡',
    card_total DECIMAL(12,2) DEFAULT 0 COMMENT '刷卡总额',
    mobile_alipay DECIMAL(12,2) DEFAULT 0 COMMENT '支付宝',
    mobile_wechat DECIMAL(12,2) DEFAULT 0 COMMENT '微信',
    mobile_total DECIMAL(12,2) DEFAULT 0 COMMENT '移动支付总额',
    credit_amount DECIMAL(12,2) DEFAULT 0 COMMENT '挂账金额',
    prepaid_amount DECIMAL(12,2) DEFAULT 0 COMMENT '预付金额',
    refund_amount DECIMAL(12,2) DEFAULT 0 COMMENT '退款金额',
    receivable_total DECIMAL(12,2) DEFAULT 0 COMMENT '应收合计',
    actual_cash DECIMAL(12,2) DEFAULT 0 COMMENT '实际现金',
    actual_card DECIMAL(12,2) DEFAULT 0 COMMENT '实际刷卡',
    actual_mobile DECIMAL(12,2) DEFAULT 0 COMMENT '实际移动支付',
    actual_total DECIMAL(12,2) DEFAULT 0 COMMENT '实际合计',
    difference DECIMAL(12,2) DEFAULT 0 COMMENT '差异金额',
    difference_reason VARCHAR(1000) COMMENT '差异原因',
    difference_proof VARCHAR(1000) COMMENT '差异证明材料',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    status TINYINT DEFAULT 0 COMMENT '状态：0-未对账，1-已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_reconcile_date (reconcile_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日结对账表';

-- =============================================
-- 6. 交接班对账表
-- =============================================
CREATE TABLE IF NOT EXISTS shift_reconciliation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '交接班对账ID',
    shift_date DATE NOT NULL COMMENT '交接班日期',
    shift_type TINYINT COMMENT '班次：1-早班，2-中班，3-晚班',
    handover_user_id BIGINT COMMENT '交班人ID',
    handover_user_name VARCHAR(50) COMMENT '交班人姓名',
    takeover_user_id BIGINT COMMENT '接班人ID',
    takeover_user_name VARCHAR(50) COMMENT '接班人姓名',
    cash_total DECIMAL(12,2) DEFAULT 0 COMMENT '现金收款',
    card_total DECIMAL(12,2) DEFAULT 0 COMMENT '刷卡收款',
    mobile_total DECIMAL(12,2) DEFAULT 0 COMMENT '移动支付收款',
    credit_total DECIMAL(12,2) DEFAULT 0 COMMENT '挂账金额',
    receivable_total DECIMAL(12,2) DEFAULT 0 COMMENT '应收合计',
    actual_cash DECIMAL(12,2) DEFAULT 0 COMMENT '实际现金',
    actual_card DECIMAL(12,2) DEFAULT 0 COMMENT '实际刷卡',
    actual_mobile DECIMAL(12,2) DEFAULT 0 COMMENT '实际移动支付',
    actual_total DECIMAL(12,2) DEFAULT 0 COMMENT '实际合计',
    difference DECIMAL(12,2) DEFAULT 0 COMMENT '差异金额',
    difference_reason VARCHAR(1000) COMMENT '差异原因',
    handover_remark VARCHAR(1000) COMMENT '交班备注',
    takeover_remark VARCHAR(1000) COMMENT '接班备注',
    takeover_confirmed TINYINT DEFAULT 0 COMMENT '接班确认：0-未确认，1-已确认',
    takeover_confirm_time DATETIME COMMENT '接班确认时间',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待确认，1-已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_shift_date (shift_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交接班对账表';

-- =============================================
-- 7. check_in表增加协议单位相关字段
-- =============================================
ALTER TABLE check_in ADD COLUMN agreement_unit_id BIGINT COMMENT '协议单位ID' AFTER remark;
ALTER TABLE check_in ADD COLUMN agreement_unit_name VARCHAR(100) COMMENT '协议单位名称' AFTER agreement_unit_id;
ALTER TABLE check_in ADD COLUMN guarantee_type TINYINT COMMENT '担保方式：1-现金，2-协议挂账' AFTER agreement_unit_name;
ALTER TABLE check_in ADD COLUMN is_credit TINYINT DEFAULT 0 COMMENT '是否挂账：0-否，1-是' AFTER guarantee_type;

-- =============================================
-- 8. 财务管理菜单 (ID range: 500-599)
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1000, '财务管理', 0, 5, '/finance', NULL, NULL, 0, 1, 1, 'Money');

-- 协议单位管理
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1001, '协议单位管理', 1000, 1, '/finance/agreement', 'finance/AgreementUnitManage', 'finance:agreement:query', 1, 1, 1, 'Connection');

-- 挂账结算
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1002, '挂账结算', 1000, 2, '/finance/settlement', 'finance/CreditSettlement', 'finance:settlement:query', 1, 1, 1, 'Document');

-- 退款审批
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1003, '退款审批', 1000, 3, '/finance/refund', 'finance/RefundApproval', 'finance:refund:query', 1, 1, 1, 'Refund');

-- 日结对账
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1004, '日结对账', 1000, 4, '/finance/daily', 'finance/DailyReconciliation', 'finance:daily:query', 1, 1, 1, 'Calendar');

-- 交接班
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1005, '交接班', 1000, 5, '/finance/shift', 'finance/ShiftReconciliation', 'finance:shift:query', 1, 1, 1, 'Switch');

-- 收款汇总
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1006, '收款汇总', 1000, 6, '/finance/summary', 'finance/PaymentSummary', 'finance:summary:query', 1, 1, 1, 'Wallet');

-- 应收账款监控
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1007, '应收账款监控', 1000, 7, '/finance/receivable', 'finance/ReceivableMonitor', 'finance:receivable:query', 1, 1, 1, 'Warning');

-- =============================================
-- 9. 协议单位管理按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1011, '协议单位查询', 1001, 1, '', NULL, 'finance:agreement:query', 2, 1, 1, NULL),
(1012, '协议单位新增', 1001, 2, '', NULL, 'finance:agreement:add', 2, 1, 1, NULL),
(1013, '协议单位编辑', 1001, 3, '', NULL, 'finance:agreement:edit', 2, 1, 1, NULL),
(1014, '协议单位删除', 1001, 4, '', NULL, 'finance:agreement:delete', 2, 1, 1, NULL);

-- =============================================
-- 10. 挂账结算按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1021, '挂账查询', 1002, 1, '', NULL, 'finance:settlement:query', 2, 1, 1, NULL),
(1022, '挂账新增', 1002, 2, '', NULL, 'finance:settlement:add', 2, 1, 1, NULL),
(1023, '挂账编辑', 1002, 3, '', NULL, 'finance:settlement:edit', 2, 1, 1, NULL),
(1024, '挂账删除', 1002, 4, '', NULL, 'finance:settlement:delete', 2, 1, 1, NULL),
(1025, '挂账结算审批', 1002, 5, '', NULL, 'finance:settlement:approve', 2, 1, 1, NULL);

-- =============================================
-- 11. 退款审批按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1031, '退款查询', 1003, 1, '', NULL, 'finance:refund:query', 2, 1, 1, NULL),
(1032, '退款申请', 1003, 2, '', NULL, 'finance:refund:add', 2, 1, 1, NULL),
(1033, '退款编辑', 1003, 3, '', NULL, 'finance:refund:edit', 2, 1, 1, NULL),
(1034, '退款删除', 1003, 4, '', NULL, 'finance:refund:delete', 2, 1, 1, NULL),
(1035, '退款审批', 1003, 5, '', NULL, 'finance:refund:approve', 2, 1, 1, NULL);

-- =============================================
-- 12. 日结对账按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1041, '日结查询', 1004, 1, '', NULL, 'finance:daily:query', 2, 1, 1, NULL),
(1042, '日结新增', 1004, 2, '', NULL, 'finance:daily:add', 2, 1, 1, NULL),
(1043, '日结编辑', 1004, 3, '', NULL, 'finance:daily:edit', 2, 1, 1, NULL),
(1044, '日结删除', 1004, 4, '', NULL, 'finance:daily:delete', 2, 1, 1, NULL),
(1045, '日结审批', 1004, 5, '', NULL, 'finance:daily:approve', 2, 1, 1, NULL);

-- =============================================
-- 13. 交接班按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1051, '交接班查询', 1005, 1, '', NULL, 'finance:shift:query', 2, 1, 1, NULL),
(1052, '交接班新增', 1005, 2, '', NULL, 'finance:shift:add', 2, 1, 1, NULL),
(1053, '交接班编辑', 1005, 3, '', NULL, 'finance:shift:edit', 2, 1, 1, NULL),
(1054, '交接班删除', 1005, 4, '', NULL, 'finance:shift:delete', 2, 1, 1, NULL),
(1055, '接班确认', 1005, 5, '', NULL, 'finance:shift:confirm', 2, 1, 1, NULL);

-- =============================================
-- 14. 收款汇总按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1061, '收款汇总查询', 1006, 1, '', NULL, 'finance:summary:query', 2, 1, 1, NULL),
(1062, '收款汇总导出', 1006, 2, '', NULL, 'finance:summary:export', 2, 1, 1, NULL);

-- =============================================
-- 15. 应收账款监控按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1071, '应收账款查询', 1007, 1, '', NULL, 'finance:receivable:query', 2, 1, 1, NULL),
(1072, '应收账款导出', 1007, 2, '', NULL, 'finance:receivable:export', 2, 1, 1, NULL);

-- =============================================
-- 16. 分配角色权限 - 超级管理员(role_id=1)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1000), (1, 1001), (1, 1002), (1, 1003), (1, 1004), (1, 1005), (1, 1006), (1, 1007),
(1, 1011), (1, 1012), (1, 1013), (1, 1014),
(1, 1021), (1, 1022), (1, 1023), (1, 1024), (1, 1025),
(1, 1031), (1, 1032), (1, 1033), (1, 1034), (1, 1035),
(1, 1041), (1, 1042), (1, 1043), (1, 1044), (1, 1045),
(1, 1051), (1, 1052), (1, 1053), (1, 1054), (1, 1055),
(1, 1061), (1, 1062),
(1, 1071), (1, 1072);

-- =============================================
-- 17. 分配角色权限 - 酒店管理员(role_id=3)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1000), (3, 1001), (3, 1002), (3, 1003), (3, 1004), (3, 1005), (3, 1006), (3, 1007),
(3, 1011), (3, 1012), (3, 1013), (3, 1014),
(3, 1021), (3, 1022), (3, 1023), (3, 1024), (3, 1025),
(3, 1031), (3, 1032), (3, 1033), (3, 1034), (3, 1035),
(3, 1041), (3, 1042), (3, 1043), (3, 1044), (3, 1045),
(3, 1051), (3, 1052), (3, 1053), (3, 1054), (3, 1055),
(3, 1061), (3, 1062),
(3, 1071), (3, 1072);

-- =============================================
-- 18. 分配角色权限 - 财务人员(role_id=7)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(7, 1000), (7, 1001), (7, 1002), (7, 1003), (7, 1004), (7, 1005), (7, 1006), (7, 1007),
(7, 1011), (7, 1012), (7, 1013),
(7, 1021), (7, 1025),
(7, 1031), (7, 1035),
(7, 1041), (7, 1042), (7, 1043), (7, 1045),
(7, 1051), (7, 1052),
(7, 1061), (7, 1062),
(7, 1071), (7, 1072);

-- =============================================
-- 19. 分配角色权限 - 前厅部经理(role_id=4)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 1000), (4, 1001), (4, 1002), (4, 1003), (4, 1005),
(4, 1011),
(4, 1021), (4, 1022), (4, 1023),
(4, 1031), (4, 1032),
(4, 1051), (4, 1052), (4, 1053), (4, 1055);

-- =============================================
-- 20. 分配角色权限 - 普通前台(role_id=6)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(6, 1000), (6, 1001), (6, 1002), (6, 1005),
(6, 1011),
(6, 1021), (6, 1022),
(6, 1051), (6, 1052);
