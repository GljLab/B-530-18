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
    address VARCHAR(500) COMMENT '单位地址',
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
    agreement_file VARCHAR(500) COMMENT '协议附件路径',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    remark VARCHAR(500) COMMENT '备注',
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
    remark VARCHAR(500) COMMENT '备注',
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
    approve_remark VARCHAR(500) COMMENT '审批意见',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
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
    difference_reason VARCHAR(500) COMMENT '差异原因',
    difference_proof VARCHAR(500) COMMENT '差异证明材料',
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
    difference_reason VARCHAR(500) COMMENT '差异原因',
    handover_remark VARCHAR(500) COMMENT '交班备注',
    takeover_remark VARCHAR(500) COMMENT '接班备注',
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
(500, '财务管理', 0, 5, '/finance', NULL, NULL, 0, 1, 1, 'Money');

-- 协议单位管理
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(501, '协议单位管理', 500, 1, '/finance/agreement', 'finance/AgreementUnitManage', 'finance:agreement:query', 1, 1, 1, 'Connection');

-- 挂账结算
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(502, '挂账结算', 500, 2, '/finance/settlement', 'finance/CreditSettlement', 'finance:settlement:query', 1, 1, 1, 'Document');

-- 退款审批
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(503, '退款审批', 500, 3, '/finance/refund', 'finance/RefundApproval', 'finance:refund:query', 1, 1, 1, 'Refund');

-- 日结对账
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(504, '日结对账', 500, 4, '/finance/daily', 'finance/DailyReconciliation', 'finance:daily:query', 1, 1, 1, 'Calendar');

-- 交接班
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(505, '交接班', 500, 5, '/finance/shift', 'finance/ShiftReconciliation', 'finance:shift:query', 1, 1, 1, 'Switch');

-- 收款汇总
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(506, '收款汇总', 500, 6, '/finance/summary', 'finance/PaymentSummary', 'finance:summary:query', 1, 1, 1, 'Wallet');

-- 应收账款监控
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(507, '应收账款监控', 500, 7, '/finance/receivable', 'finance/ReceivableMonitor', 'finance:receivable:query', 1, 1, 1, 'Warning');

-- =============================================
-- 9. 协议单位管理按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(511, '协议单位查询', 501, 1, '', NULL, 'finance:agreement:query', 2, 1, 1, NULL),
(512, '协议单位新增', 501, 2, '', NULL, 'finance:agreement:add', 2, 1, 1, NULL),
(513, '协议单位编辑', 501, 3, '', NULL, 'finance:agreement:edit', 2, 1, 1, NULL),
(514, '协议单位删除', 501, 4, '', NULL, 'finance:agreement:delete', 2, 1, 1, NULL);

-- =============================================
-- 10. 挂账结算按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(521, '挂账查询', 502, 1, '', NULL, 'finance:settlement:query', 2, 1, 1, NULL),
(522, '挂账新增', 502, 2, '', NULL, 'finance:settlement:add', 2, 1, 1, NULL),
(523, '挂账编辑', 502, 3, '', NULL, 'finance:settlement:edit', 2, 1, 1, NULL),
(524, '挂账删除', 502, 4, '', NULL, 'finance:settlement:delete', 2, 1, 1, NULL),
(525, '挂账结算审批', 502, 5, '', NULL, 'finance:settlement:approve', 2, 1, 1, NULL);

-- =============================================
-- 11. 退款审批按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(531, '退款查询', 503, 1, '', NULL, 'finance:refund:query', 2, 1, 1, NULL),
(532, '退款申请', 503, 2, '', NULL, 'finance:refund:add', 2, 1, 1, NULL),
(533, '退款编辑', 503, 3, '', NULL, 'finance:refund:edit', 2, 1, 1, NULL),
(534, '退款删除', 503, 4, '', NULL, 'finance:refund:delete', 2, 1, 1, NULL),
(535, '退款审批', 503, 5, '', NULL, 'finance:refund:approve', 2, 1, 1, NULL);

-- =============================================
-- 12. 日结对账按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(541, '日结查询', 504, 1, '', NULL, 'finance:daily:query', 2, 1, 1, NULL),
(542, '日结新增', 504, 2, '', NULL, 'finance:daily:add', 2, 1, 1, NULL),
(543, '日结编辑', 504, 3, '', NULL, 'finance:daily:edit', 2, 1, 1, NULL),
(544, '日结删除', 504, 4, '', NULL, 'finance:daily:delete', 2, 1, 1, NULL),
(545, '日结审批', 504, 5, '', NULL, 'finance:daily:approve', 2, 1, 1, NULL);

-- =============================================
-- 13. 交接班按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(551, '交接班查询', 505, 1, '', NULL, 'finance:shift:query', 2, 1, 1, NULL),
(552, '交接班新增', 505, 2, '', NULL, 'finance:shift:add', 2, 1, 1, NULL),
(553, '交接班编辑', 505, 3, '', NULL, 'finance:shift:edit', 2, 1, 1, NULL),
(554, '交接班删除', 505, 4, '', NULL, 'finance:shift:delete', 2, 1, 1, NULL),
(555, '接班确认', 505, 5, '', NULL, 'finance:shift:confirm', 2, 1, 1, NULL);

-- =============================================
-- 14. 收款汇总按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(561, '收款汇总查询', 506, 1, '', NULL, 'finance:summary:query', 2, 1, 1, NULL),
(562, '收款汇总导出', 506, 2, '', NULL, 'finance:summary:export', 2, 1, 1, NULL);

-- =============================================
-- 15. 应收账款监控按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(571, '应收账款查询', 507, 1, '', NULL, 'finance:receivable:query', 2, 1, 1, NULL),
(572, '应收账款导出', 507, 2, '', NULL, 'finance:receivable:export', 2, 1, 1, NULL);

-- =============================================
-- 16. 分配角色权限 - 超级管理员(role_id=1)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 500), (1, 501), (1, 502), (1, 503), (1, 504), (1, 505), (1, 506), (1, 507),
(1, 511), (1, 512), (1, 513), (1, 514),
(1, 521), (1, 522), (1, 523), (1, 524), (1, 525),
(1, 531), (1, 532), (1, 533), (1, 534), (1, 535),
(1, 541), (1, 542), (1, 543), (1, 544), (1, 545),
(1, 551), (1, 552), (1, 553), (1, 554), (1, 555),
(1, 561), (1, 562),
(1, 571), (1, 572);

-- =============================================
-- 17. 分配角色权限 - 酒店管理员(role_id=3)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 500), (3, 501), (3, 502), (3, 503), (3, 504), (3, 505), (3, 506), (3, 507),
(3, 511), (3, 512), (3, 513), (3, 514),
(3, 521), (3, 522), (3, 523), (3, 524), (3, 525),
(3, 531), (3, 532), (3, 533), (3, 534), (3, 535),
(3, 541), (3, 542), (3, 543), (3, 544), (3, 545),
(3, 551), (3, 552), (3, 553), (3, 554), (3, 555),
(3, 561), (3, 562),
(3, 571), (3, 572);

-- =============================================
-- 18. 分配角色权限 - 财务人员(role_id=7)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(7, 500), (7, 501), (7, 502), (7, 503), (7, 504), (7, 505), (7, 506), (7, 507),
(7, 511), (7, 512), (7, 513),
(7, 521), (7, 525),
(7, 531), (7, 535),
(7, 541), (7, 542), (7, 543), (7, 545),
(7, 551), (7, 552),
(7, 561), (7, 562),
(7, 571), (7, 572);

-- =============================================
-- 19. 分配角色权限 - 前厅部经理(role_id=4)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 500), (4, 501), (4, 502), (4, 503), (4, 505),
(4, 511),
(4, 521), (4, 522), (4, 523),
(4, 531), (4, 532),
(4, 551), (4, 552), (4, 553), (4, 555);

-- =============================================
-- 20. 分配角色权限 - 普通前台(role_id=6)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(6, 500), (6, 501), (6, 502), (6, 505),
(6, 511),
(6, 521), (6, 522),
(6, 551), (6, 552);
