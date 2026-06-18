SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
-- =============================================
-- 高级财务分析管理系统数据库迁移
-- 功能：发票管理、收款分析、坏账处理、综合财务分析、财务看板
-- =============================================

USE permission_system;

-- =============================================
-- 1. 发票管理表
-- =============================================
CREATE TABLE IF NOT EXISTS invoice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '发票ID',
    invoice_no VARCHAR(50) COMMENT '发票编号(系统生成)',
    related_order_type TINYINT COMMENT '关联单类型：1-入住单，2-预订单',
    related_order_id BIGINT COMMENT '关联单ID',
    related_order_no VARCHAR(50) COMMENT '关联单号',
    customer_id BIGINT COMMENT '客户ID',
    customer_name VARCHAR(50) COMMENT '客户姓名',
    invoice_type TINYINT NOT NULL COMMENT '发票类型：1-普通发票(个人)，2-普通发票(企业)，3-专用发票',
    title_type TINYINT COMMENT '抬头类型：1-个人，2-企业',
    invoice_title VARCHAR(200) NOT NULL COMMENT '发票抬头',
    tax_no VARCHAR(50) COMMENT '税号',
    company_address VARCHAR(200) COMMENT '企业地址电话',
    bank_account VARCHAR(200) COMMENT '开户行账号',
    invoice_amount DECIMAL(12,2) NOT NULL COMMENT '开票金额',
    order_amount DECIMAL(12,2) COMMENT '订单金额',
    invoice_item TINYINT DEFAULT 1 COMMENT '开票项目：1-住宿费，2-餐饮费，3-其他',
    contact_phone VARCHAR(20) COMMENT '客人联系电话',
    contact_email VARCHAR(100) COMMENT '客人邮箱(电子发票)',
    invoice_code VARCHAR(50) COMMENT '发票代码',
    invoice_number VARCHAR(50) COMMENT '发票号码',
    invoice_pdf VARCHAR(1000) COMMENT 'PDF扫描件路径',
    is_electronic TINYINT DEFAULT 0 COMMENT '是否电子发票：0-否，1-是',
    email_sent TINYINT DEFAULT 0 COMMENT '电子发票是否已发送邮箱：0-否，1-是',
    mail_recipient VARCHAR(50) COMMENT '邮寄收件人',
    mail_address VARCHAR(500) COMMENT '邮寄地址',
    mail_company VARCHAR(100) COMMENT '快递公司',
    mail_tracking_no VARCHAR(50) COMMENT '快递单号',
    mail_time DATETIME COMMENT '邮寄时间',
    applicant_id BIGINT COMMENT '申请人ID',
    applicant_name VARCHAR(50) COMMENT '申请人姓名',
    apply_time DATETIME COMMENT '申请时间',
    processor_id BIGINT COMMENT '处理人ID',
    processor_name VARCHAR(50) COMMENT '处理人姓名',
    process_time DATETIME COMMENT '处理时间',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待开票，1-已开票，2-待邮寄，3-已邮寄，4-已完成',
    remark VARCHAR(1000) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_invoice_no (invoice_no),
    INDEX idx_customer_name (customer_name),
    INDEX idx_related_order (related_order_type, related_order_id),
    INDEX idx_status (status),
    INDEX idx_invoice_type (invoice_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发票管理表';

-- =============================================
-- 2. 坏账表
-- =============================================
CREATE TABLE IF NOT EXISTS bad_debt (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '坏账ID',
    debt_no VARCHAR(50) NOT NULL UNIQUE COMMENT '坏账编号',
    related_order_type TINYINT COMMENT '关联单类型：1-入住单，2-预订单',
    related_order_id BIGINT COMMENT '关联单ID',
    related_order_no VARCHAR(50) COMMENT '关联单号',
    customer_id BIGINT COMMENT '客户ID',
    customer_name VARCHAR(50) COMMENT '客户姓名',
    debt_type TINYINT NOT NULL COMMENT '坏账类型：1-挂账超期，2-逃单，3-拖欠费用',
    original_amount DECIMAL(12,2) NOT NULL COMMENT '原始欠款金额',
    recovered_amount DECIMAL(12,2) DEFAULT 0 COMMENT '已收回金额',
    remaining_amount DECIMAL(12,2) DEFAULT 0 COMMENT '剩余欠款金额',
    debt_time DATETIME COMMENT '欠款发生时间',
    overdue_days INT DEFAULT 0 COMMENT '逾期天数',
    status TINYINT DEFAULT 0 COMMENT '状态：0-疑似坏账，1-催收中，2-已核销，3-追偿中，4-部分收回，5-已关闭',
    write_off_reason VARCHAR(1000) COMMENT '核销原因',
    write_off_proof VARCHAR(1000) COMMENT '核销证明材料',
    write_off_approver_id BIGINT COMMENT '核销审批人ID',
    write_off_approver_name VARCHAR(50) COMMENT '核销审批人姓名',
    write_off_approve_time DATETIME COMMENT '核销审批时间',
    legal_status TINYINT COMMENT '法律追偿状态：0-未启动，1-律师函，2-诉讼中，3-执行中，4-已结案',
    legal_info VARCHAR(1000) COMMENT '法律追偿信息(JSON)',
    remark VARCHAR(1000) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_debt_no (debt_no),
    INDEX idx_customer_name (customer_name),
    INDEX idx_status (status),
    INDEX idx_debt_type (debt_type),
    INDEX idx_debt_time (debt_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='坏账表';

-- =============================================
-- 3. 坏账处理记录表
-- =============================================
CREATE TABLE IF NOT EXISTS bad_debt_action (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    bad_debt_id BIGINT NOT NULL COMMENT '坏账ID',
    action_type TINYINT NOT NULL COMMENT '操作类型：1-催收，2-部分收回，3-核销，4-法律追偿，5-关闭',
    action_time DATETIME NOT NULL COMMENT '操作时间',
    action_method TINYINT COMMENT '催收方式：1-电话，2-短信，3-上门',
    action_result VARCHAR(500) COMMENT '操作结果反馈',
    recovered_amount DECIMAL(12,2) COMMENT '收回金额',
    payment_method TINYINT COMMENT '收款方式：1-现金，2-刷卡，3-移动支付，4-转账',
    payment_time DATETIME COMMENT '收款时间',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    remark VARCHAR(1000) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_bad_debt_id (bad_debt_id),
    INDEX idx_action_type (action_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='坏账处理记录表';

-- =============================================
-- 4. 高级财务分析菜单 (ID range: 1100-1199)
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1100, '发票管理', 1000, 8, '/finance/invoice', 'finance/InvoiceManage', 'finance:invoice:query', 1, 1, 1, 'Ticket');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1110, '收款明细报表', 1000, 9, '/finance/collectionDetail', 'finance/CollectionDetail', 'finance:collection:query', 1, 1, 1, 'List');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1111, '收款趋势分析', 1000, 10, '/finance/collectionTrend', 'finance/CollectionTrend', 'finance:collection:query', 1, 1, 1, 'TrendCharts');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1112, '支付方式分析', 1000, 11, '/finance/paymentMethod', 'finance/PaymentMethodAnalysis', 'finance:collection:query', 1, 1, 1, 'PieChart');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1113, '收款人员统计', 1000, 12, '/finance/cashierStats', 'finance/CashierStatistics', 'finance:cashier:query', 1, 1, 1, 'User');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1120, '坏账管理', 1000, 13, '/finance/badDebt', 'finance/BadDebtManage', 'finance:badDebt:query', 1, 1, 1, 'Warning');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1130, '营收分析', 1000, 14, '/finance/revenueAnalysis', 'finance/RevenueAnalysis', 'finance:revenue:query', 1, 1, 1, 'Coin');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1131, '应收账款分析', 1000, 15, '/finance/receivableAnalysis', 'finance/ReceivableAnalysis', 'finance:receivable:query', 1, 1, 1, 'DataLine');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1132, '现金流分析', 1000, 16, '/finance/cashFlow', 'finance/CashFlowAnalysis', 'finance:cashflow:query', 1, 1, 1, 'Money');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1140, '财务总览看板', 1000, 17, '/finance/dashboard', 'finance/FinanceDashboard', 'finance:dashboard:query', 1, 1, 1, 'Monitor');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1141, '对比分析', 1000, 18, '/finance/comparison', 'finance/ComparisonAnalysis', 'finance:comparison:query', 1, 1, 1, 'DataAnalysis');

-- =============================================
-- 5. 发票管理按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1101, '发票查询', 1100, 1, '', NULL, 'finance:invoice:query', 2, 1, 1, NULL),
(1102, '发票申请', 1100, 2, '', NULL, 'finance:invoice:add', 2, 1, 1, NULL),
(1103, '发票处理', 1100, 3, '', NULL, 'finance:invoice:process', 2, 1, 1, NULL),
(1104, '发票邮寄', 1100, 4, '', NULL, 'finance:invoice:mail', 2, 1, 1, NULL),
(1105, '发票导出', 1100, 5, '', NULL, 'finance:invoice:export', 2, 1, 1, NULL);

-- =============================================
-- 6. 收款分析按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1115, '收款明细查询', 1110, 1, '', NULL, 'finance:collection:query', 2, 1, 1, NULL),
(1116, '收款明细导出', 1110, 2, '', NULL, 'finance:collection:export', 2, 1, 1, NULL);

-- =============================================
-- 7. 收款人员统计按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1117, '收款人员查询', 1113, 1, '', NULL, 'finance:cashier:query', 2, 1, 1, NULL),
(1118, '收款人员导出', 1113, 2, '', NULL, 'finance:cashier:export', 2, 1, 1, NULL);

-- =============================================
-- 8. 坏账管理按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1121, '坏账查询', 1120, 1, '', NULL, 'finance:badDebt:query', 2, 1, 1, NULL),
(1122, '坏账催收', 1120, 2, '', NULL, 'finance:badDebt:collect', 2, 1, 1, NULL),
(1123, '坏账部分收回', 1120, 3, '', NULL, 'finance:badDebt:recover', 2, 1, 1, NULL),
(1124, '坏账核销', 1120, 4, '', NULL, 'finance:badDebt:writeoff', 2, 1, 1, NULL),
(1125, '坏账法律追偿', 1120, 5, '', NULL, 'finance:badDebt:legal', 2, 1, 1, NULL),
(1126, '坏账导出', 1120, 6, '', NULL, 'finance:badDebt:export', 2, 1, 1, NULL);

-- =============================================
-- 9. 营收分析按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1133, '营收分析查询', 1130, 1, '', NULL, 'finance:revenue:query', 2, 1, 1, NULL),
(1134, '营收分析导出', 1130, 2, '', NULL, 'finance:revenue:export', 2, 1, 1, NULL);

-- =============================================
-- 10. 成本利润分析按钮权限(字段预留)
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1135, '成本利润查询', 1130, 3, '', NULL, 'finance:costprofit:query', 2, 1, 1, NULL);

-- =============================================
-- 11. 应收账款分析按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1136, '应收分析查询', 1131, 1, '', NULL, 'finance:receivable:query', 2, 1, 1, NULL),
(1137, '应收分析导出', 1131, 2, '', NULL, 'finance:receivable:export', 2, 1, 1, NULL);

-- =============================================
-- 12. 现金流分析按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1138, '现金流查询', 1132, 1, '', NULL, 'finance:cashflow:query', 2, 1, 1, NULL),
(1139, '现金流导出', 1132, 2, '', NULL, 'finance:cashflow:export', 2, 1, 1, NULL);

-- =============================================
-- 13. 财务看板按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1142, '财务看板查询', 1140, 1, '', NULL, 'finance:dashboard:query', 2, 1, 1, NULL);

-- =============================================
-- 14. 对比分析按钮权限
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1143, '对比分析查询', 1141, 1, '', NULL, 'finance:comparison:query', 2, 1, 1, NULL),
(1144, '对比分析导出', 1141, 2, '', NULL, 'finance:comparison:export', 2, 1, 1, NULL);

-- =============================================
-- 15. 分配角色权限 - 超级管理员(role_id=1)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1100), (1, 1101), (1, 1102), (1, 1103), (1, 1104), (1, 1105),
(1, 1110), (1, 1111), (1, 1112), (1, 1113), (1, 1115), (1, 1116), (1, 1117), (1, 1118),
(1, 1120), (1, 1121), (1, 1122), (1, 1123), (1, 1124), (1, 1125), (1, 1126),
(1, 1130), (1, 1131), (1, 1132), (1, 1133), (1, 1134), (1, 1135), (1, 1136), (1, 1137), (1, 1138), (1, 1139),
(1, 1140), (1, 1141), (1, 1142), (1, 1143), (1, 1144);

-- =============================================
-- 16. 分配角色权限 - 酒店管理员(role_id=3): 所有权限
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1100), (3, 1101), (3, 1102), (3, 1103), (3, 1104), (3, 1105),
(3, 1110), (3, 1111), (3, 1112), (3, 1113), (3, 1115), (3, 1116), (3, 1117), (3, 1118),
(3, 1120), (3, 1121), (3, 1122), (3, 1123), (3, 1124), (3, 1125), (3, 1126),
(3, 1130), (3, 1131), (3, 1132), (3, 1133), (3, 1134), (3, 1135), (3, 1136), (3, 1137), (3, 1138), (3, 1139),
(3, 1140), (3, 1141), (3, 1142), (3, 1143), (3, 1144);

-- =============================================
-- 17. 分配角色权限 - 前厅部经理(role_id=4): 查看营收/收款/应收，不能查看成本利润
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 1100), (4, 1101),
(4, 1110), (4, 1111), (4, 1112), (4, 1113), (4, 1115),
(4, 1120), (4, 1121),
(4, 1130), (4, 1131), (4, 1132), (4, 1133), (4, 1136), (4, 1138),
(4, 1140), (4, 1141), (4, 1142), (4, 1143);

-- =============================================
-- 18. 分配角色权限 - 普通前台(role_id=6): 只能查看自己的收款统计
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(6, 1100), (6, 1101), (6, 1102),
(6, 1113), (6, 1117),
(6, 1120), (6, 1121);

-- =============================================
-- 19. 分配角色权限 - 财务人员(role_id=7): 发票管理、坏账处理、查看所有分析、导出
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(7, 1100), (7, 1101), (7, 1102), (7, 1103), (7, 1104), (7, 1105),
(7, 1110), (7, 1111), (7, 1112), (7, 1113), (7, 1115), (7, 1116), (7, 1117), (7, 1118),
(7, 1120), (7, 1121), (7, 1122), (7, 1123), (7, 1124), (7, 1125), (7, 1126),
(7, 1130), (7, 1131), (7, 1132), (7, 1133), (7, 1134), (7, 1136), (7, 1137), (7, 1138), (7, 1139),
(7, 1140), (7, 1141), (7, 1142), (7, 1143), (7, 1144);

-- =============================================
-- 20. 新增财务经理角色(role_id=8): 所有财务权限+审批坏账核销+查看成本利润
-- =============================================
INSERT INTO sys_role (id, role_name, role_key, status, order_num, remark) VALUES
(8, '财务经理', 'finance_manager', 1, 7, '财务经理，拥有所有财务权限，可审批坏账核销，查看成本利润')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(8, 1000), (8, 1001), (8, 1002), (8, 1003), (8, 1004), (8, 1005), (8, 1006), (8, 1007),
(8, 1011), (8, 1012), (8, 1013),
(8, 1021), (8, 1025),
(8, 1031), (8, 1035),
(8, 1041), (8, 1042), (8, 1043), (8, 1045),
(8, 1051), (8, 1052),
(8, 1061), (8, 1062),
(8, 1071), (8, 1072),
(8, 1100), (8, 1101), (8, 1102), (8, 1103), (8, 1104), (8, 1105),
(8, 1110), (8, 1111), (8, 1112), (8, 1113), (8, 1115), (8, 1116), (8, 1117), (8, 1118),
(8, 1120), (8, 1121), (8, 1122), (8, 1123), (8, 1124), (8, 1125), (8, 1126),
(8, 1130), (8, 1131), (8, 1132), (8, 1133), (8, 1134), (8, 1135), (8, 1136), (8, 1137), (8, 1138), (8, 1139),
(8, 1140), (8, 1141), (8, 1142), (8, 1143), (8, 1144);

-- =============================================
-- 21. 创建财务经理测试账号
-- =============================================
INSERT INTO sys_user (username, password, nickname, phone, email, status, avatar)
VALUES ('finance_manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '财务经理', '13800000008', 'finance_manager@hotel.com', 1, NULL)
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname);

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, 8 FROM sys_user u WHERE u.username = 'finance_manager'
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);
