SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE permission_system;

-- =============================================
-- 投诉处理系统
-- =============================================

-- =============================================
-- 1. 投诉主表
-- =============================================
DROP TABLE IF EXISTS complaint;
CREATE TABLE complaint (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  complaint_no VARCHAR(32) NOT NULL COMMENT '投诉单号 CP+年月日+4位序号',
  check_in_id BIGINT DEFAULT NULL COMMENT '关联入住单ID',
  check_in_no VARCHAR(32) DEFAULT NULL COMMENT '入住单号',
  customer_id BIGINT DEFAULT NULL COMMENT '客人ID',
  customer_name VARCHAR(50) NOT NULL COMMENT '客人姓名',
  customer_phone VARCHAR(20) NOT NULL COMMENT '客人手机号',
  customer_email VARCHAR(100) DEFAULT NULL COMMENT '客人邮箱',
  room_type_id BIGINT DEFAULT NULL COMMENT '房型ID',
  room_type_name VARCHAR(50) DEFAULT NULL COMMENT '房型名称',
  check_in_date DATE DEFAULT NULL COMMENT '入住日期',
  check_out_date DATE DEFAULT NULL COMMENT '退房日期',
  complaint_type TINYINT NOT NULL COMMENT '投诉类型:1-服务质量问题,2-设施设备问题,3-卫生问题,4-安全问题,5-价格收费问题,6-员工态度问题,7-噪音问题,8-其他',
  complaint_content VARCHAR(500) NOT NULL COMMENT '投诉内容',
  expected_solution VARCHAR(100) DEFAULT NULL COMMENT '期望解决方案',
  complaint_status TINYINT NOT NULL DEFAULT 1 COMMENT '投诉状态:1-待处理,2-处理中,3-已处理,4-已驳回',
  verify_code VARCHAR(16) DEFAULT NULL COMMENT '验证码(用于H5链接访问)',
  accept_user_id BIGINT DEFAULT NULL COMMENT '受理人ID',
  accept_user_name VARCHAR(50) DEFAULT NULL COMMENT '受理人姓名',
  accept_time DATETIME DEFAULT NULL COMMENT '受理时间',
  accept_remark VARCHAR(200) DEFAULT NULL COMMENT '受理意见',
  assign_user_id BIGINT DEFAULT NULL COMMENT '分配责任人ID',
  assign_user_name VARCHAR(50) DEFAULT NULL COMMENT '分配责任人姓名',
  reject_reason TINYINT DEFAULT NULL COMMENT '驳回原因:1-投诉不成立,2-信息不完整,3-超出受理范围,4-其他',
  reject_remark VARCHAR(200) DEFAULT NULL COMMENT '驳回详细说明',
  reject_user_id BIGINT DEFAULT NULL COMMENT '驳回人ID',
  reject_user_name VARCHAR(50) DEFAULT NULL COMMENT '驳回人姓名',
  reject_time DATETIME DEFAULT NULL COMMENT '驳回时间',
  handle_solution VARCHAR(500) DEFAULT NULL COMMENT '处理方案',
  handle_result TINYINT DEFAULT NULL COMMENT '处理结果:1-已解决,2-部分解决,3-无法解决',
  compensation_plan VARCHAR(100) DEFAULT NULL COMMENT '补偿方案',
  handle_remark VARCHAR(200) DEFAULT NULL COMMENT '处理备注',
  handle_user_id BIGINT DEFAULT NULL COMMENT '处理人ID',
  handle_user_name VARCHAR(50) DEFAULT NULL COMMENT '处理人姓名',
  handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
  need_reprocess TINYINT NOT NULL DEFAULT 0 COMMENT '是否需再处理:0-否,1-是',
  complaint_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '投诉时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记:0-未删除,1-已删除',
  UNIQUE KEY uk_complaint_no (complaint_no, deleted),
  KEY idx_check_in_id (check_in_id),
  KEY idx_customer_phone (customer_phone),
  KEY idx_complaint_status (complaint_status),
  KEY idx_complaint_type (complaint_type),
  KEY idx_complaint_time (complaint_time),
  KEY idx_assign_user_id (assign_user_id),
  KEY idx_verify_code (verify_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投诉主表';

-- =============================================
-- 2. 投诉图片表
-- =============================================
DROP TABLE IF EXISTS complaint_image;
CREATE TABLE complaint_image (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  complaint_id BIGINT NOT NULL COMMENT '投诉ID',
  image_url VARCHAR(500) NOT NULL COMMENT '图片URL',
  image_name VARCHAR(200) DEFAULT NULL COMMENT '图片名称',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记:0-未删除,1-已删除',
  KEY idx_complaint_id (complaint_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投诉图片表';

-- =============================================
-- 3. 投诉回访记录表
-- =============================================
DROP TABLE IF EXISTS complaint_visit;
CREATE TABLE complaint_visit (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  complaint_id BIGINT NOT NULL COMMENT '投诉ID',
  visit_time DATETIME NOT NULL COMMENT '回访时间',
  visit_method TINYINT NOT NULL COMMENT '回访方式:1-电话,2-短信,3-邮件',
  satisfaction TINYINT NOT NULL COMMENT '客人满意度:1-满意,2-基本满意,3-不满意',
  visit_remark VARCHAR(200) NOT NULL COMMENT '回访备注',
  visit_user_id BIGINT NOT NULL COMMENT '回访人ID',
  visit_user_name VARCHAR(50) NOT NULL COMMENT '回访人姓名',
  need_reprocess TINYINT NOT NULL DEFAULT 0 COMMENT '是否需再处理:0-否,1-是',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记:0-未删除,1-已删除',
  KEY idx_complaint_id (complaint_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投诉回访记录表';

-- =============================================
-- 4. 投诉管理菜单 (ID range: 1500-1549)
-- =============================================
-- 父菜单
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1500, '投诉管理', 0, 16, NULL, NULL, '', 1, 1, 1, 'Warning');

-- 投诉管理页面
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1501, '投诉处理', 1500, 1, '/complaint/list', 'complaint/ComplaintManage', 'complaint:list', 1, 1, 1, 'Document');

-- =============================================
-- 5. 按钮权限 - 投诉管理
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1510, '投诉查询', 1501, 1, '', NULL, 'complaint:query', 2, 1, 1, NULL),
(1511, '投诉受理', 1501, 2, '', NULL, 'complaint:accept', 2, 1, 1, NULL),
(1512, '投诉驳回', 1501, 3, '', NULL, 'complaint:reject', 2, 1, 1, NULL),
(1513, '投诉处理', 1501, 4, '', NULL, 'complaint:handle', 2, 1, 1, NULL),
(1514, '投诉回访', 1501, 5, '', NULL, 'complaint:visit', 2, 1, 1, NULL),
(1515, '我的投诉任务', 1501, 6, '', NULL, 'complaint:myTask', 2, 1, 1, NULL);

-- =============================================
-- 6. 分配角色权限 - 超级管理员(role_id=1)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1500), (1, 1501),
(1, 1510), (1, 1511), (1, 1512), (1, 1513), (1, 1514), (1, 1515);

-- =============================================
-- 7. 分配角色权限 - 酒店管理员(role_id=3): 所有投诉管理权限
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1500), (3, 1501),
(3, 1510), (3, 1511), (3, 1512), (3, 1513), (3, 1514), (3, 1515);

-- =============================================
-- 8. 分配角色权限 - 前厅部经理(role_id=4): 受理投诉、分配责任人、查看所有投诉
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 1500), (4, 1501),
(4, 1510), (4, 1511), (4, 1512);

-- =============================================
-- 9. 分配角色权限 - 客服人员(role_id=12): 受理投诉、回访投诉、查看所有投诉
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(12, 1500), (12, 1501),
(12, 1510), (12, 1511), (12, 1512), (12, 1514);

-- =============================================
-- 10. 分配角色权限 - 部门主管(role_id=13): 查看和处理分配给自己的投诉
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(13, 1500), (13, 1501),
(13, 1510), (13, 1513), (13, 1515);

-- =============================================
-- 11. 分配角色权限 - 前台员工(role_id=6): 查看投诉列表，不能受理和处理
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(6, 1500), (6, 1501),
(6, 1510);

-- =============================================
-- 12. 预置测试数据 - 投诉记录
-- =============================================
-- 待处理投诉
INSERT INTO complaint (complaint_no, check_in_id, check_in_no, customer_id, customer_name, customer_phone, customer_email, room_type_id, room_type_name, check_in_date, check_out_date, complaint_type, complaint_content, expected_solution, complaint_status, verify_code, complaint_time) VALUES
('CP202606200001', 1, 'CI20260620001', 1, '张三', '13800138001', 'zhangsan@example.com', 1, '豪华大床房', '2026-06-18', '2026-06-20', 1, '前台办理入住时态度非常不好，等待了很长时间才办理完成，服务人员全程面无表情，也没有主动说明酒店设施和早餐时间。', '希望道歉并改善服务态度', 1, 'CP8K2M', '2026-06-20 10:30:00'),
('CP202606200002', 2, 'CI20260620002', 2, '李四', '13800138002', 'lisi@example.com', 2, '标准双床房', '2026-06-19', '2026-06-20', 2, '房间空调无法制冷，晚上很热无法入睡，通知前台后维修人员很久才来，而且也没修好。卫生间淋浴头漏水。', '希望尽快修好设施，或者换房并给予补偿', 1, 'CP5N7P', '2026-06-20 14:20:00'),
('CP202606210003', 3, 'CI20260621001', 3, '王五', '13800138003', NULL, 1, '豪华大床房', '2026-06-20', '2026-06-21', 3, '房间卫生很差，床单上有明显污渍，桌面灰尘很厚，卫生间有异味。住了一晚上身上很痒，怀疑有螨虫。', '全额退款并赔偿精神损失', 1, 'CP3R9T', '2026-06-21 09:15:00');

-- 处理中投诉
INSERT INTO complaint (complaint_no, check_in_id, check_in_no, customer_id, customer_name, customer_phone, customer_email, room_type_id, room_type_name, check_in_date, check_out_date, complaint_type, complaint_content, expected_solution, complaint_status, verify_code, accept_user_id, accept_user_name, accept_time, accept_remark, assign_user_id, assign_user_name, complaint_time) VALUES
('CP202606210004', 4, 'CI20260621002', 4, '赵六', '13800138004', 'zhaoliu@example.com', 3, '行政套房', '2026-06-20', '2026-06-21', 5, '账单收费有问题，明明是会员应该打9折，但实际是原价收费。而且早餐收费也多算了一份。联系前台后一直没有解决。', '退还多收费用并给予补偿', 2, 'CP7H4J', 3, '酒店管理员', '2026-06-21 11:00:00', '请财务部核实账单，确认多收费用后退还。', 12, '客服人员', '2026-06-21 10:45:00'),
('CP202606210005', 5, 'CI20260621003', 5, '钱七', '13800138005', NULL, 2, '标准双床房', '2026-06-19', '2026-06-21', 4, '晚上房间门口有人徘徊，感觉很不安全。向保安反映后没有及时处理。走廊监控有死角。', '加强安保措施并给出合理解释', 2, 'CP2W6Y', 3, '酒店管理员', '2026-06-21 15:30:00', '请安保部门调查并加强巡逻，给客人一个满意的答复。', 13, '部门主管', '2026-06-21 15:00:00');

-- 已处理投诉
INSERT INTO complaint (complaint_no, check_in_id, check_in_no, customer_id, customer_name, customer_phone, customer_email, room_type_id, room_type_name, check_in_date, check_out_date, complaint_type, complaint_content, expected_solution, complaint_status, verify_code, accept_user_id, accept_user_name, accept_time, accept_remark, assign_user_id, assign_user_name, handle_solution, handle_result, compensation_plan, handle_remark, handle_user_id, handle_user_name, handle_time, complaint_time) VALUES
('CP202606190006', 6, 'CI20260619001', 6, '孙八', '13800138006', 'sunba@example.com', 1, '豪华大床房', '2026-06-17', '2026-06-19', 7, '隔壁房间晚上很吵，大声喧哗到凌晨一两点，多次向前台反映但没有得到有效解决，严重影响休息。', '希望能安静一些，或者换到安静的楼层', 3, 'CP9L3Q', 12, '客服人员', '2026-06-19 08:30:00', '请客房部核实情况并处理。', 13, '部门主管', '1. 已联系隔壁房间客人进行提醒和劝阻；2. 给客人换到了安静的高层房间；3. 向客人真诚道歉；4. 加强夜间巡逻和噪音管控。', 1, '赠送价值200元优惠券', '客人对处理结果表示满意，噪音问题已得到解决。', 13, '部门主管', '2026-06-19 16:00:00', '2026-06-19 07:50:00'),
('CP202606200007', 7, 'CI20260620003', 7, '周九', '13800138007', NULL, 2, '标准双床房', '2026-06-18', '2026-06-20', 6, '餐厅服务员态度恶劣，询问菜品时非常不耐烦，上菜时还把汤洒在了客人身上，没有道歉反而怪客人没坐好。', '要求涉事员工当面道歉', 3, 'CP1X5V', 3, '酒店管理员', '2026-06-20 13:00:00', '请餐饮部负责人严肃处理，务必给客人一个满意的答复。', 13, '部门主管', '1. 已找到涉事服务员进行批评教育；2. 服务员已向客人当面道歉；3. 餐饮部已召开服务质量专项会议；4. 对涉事员工进行服务态度再培训。', 1, '赠送当晚晚餐免单', '客人接受了道歉，对处理结果表示满意。', 13, '部门主管', '2026-06-20 18:30:00', '2026-06-20 12:30:00');

-- 已驳回投诉
INSERT INTO complaint (complaint_no, check_in_id, check_in_no, customer_id, customer_name, customer_phone, customer_email, room_type_id, room_type_name, check_in_date, check_out_date, complaint_type, complaint_content, expected_solution, complaint_status, verify_code, reject_reason, reject_remark, reject_user_id, reject_user_name, reject_time, complaint_time) VALUES
('CP202606200008', 8, 'CI20260620004', 8, '吴十', '13800138008', NULL, 1, '豪华大床房', '2026-06-15', '2026-06-17', 8, '投诉要求赔偿，但是没有说清楚具体是什么问题，只说酒店服务不好，问具体情况又说不上来。', '赔偿一万元', 4, 'CP6B8N', 2, '投诉信息不完整，无法核实具体情况。客人无法提供具体的时间、地点、人物等信息，也没有提供任何证据。经过多次联系仍无法补充有效信息，故驳回。', 3, '酒店管理员', '2026-06-20 16:00:00', '2026-06-20 10:00:00');

-- =============================================
-- 13. 预置测试数据 - 投诉图片
-- =============================================
INSERT INTO complaint_image (complaint_id, image_url, image_name, sort_order) VALUES
(2, '/uploads/complaint/air_conditioner.jpg', '空调故障照片', 1),
(2, '/uploads/complaint/shower_leak.jpg', '淋浴头漏水照片', 2),
(3, '/uploads/complaint/sheet_stain.jpg', '床单污渍照片', 1),
(3, '/uploads/complaint/dusty_table.jpg', '桌面灰尘照片', 2),
(3, '/uploads/complaint/bathroom_smell.jpg', '卫生间照片', 3);

-- =============================================
-- 14. 预置测试数据 - 投诉回访记录
-- =============================================
INSERT INTO complaint_visit (complaint_id, visit_time, visit_method, satisfaction, visit_remark, visit_user_id, visit_user_name, need_reprocess) VALUES
(6, '2026-06-20 10:00:00', 1, 1, '电话回访客人，客人表示已换到安静的房间，噪音问题已解决，对酒店的处理态度和效率表示满意。', 12, '客服人员', 0),
(7, '2026-06-21 09:30:00', 1, 2, '电话回访客人，客人表示已接受道歉，但希望酒店能加强员工培训，避免类似情况再次发生。基本满意。', 12, '客服人员', 0);
