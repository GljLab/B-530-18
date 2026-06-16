SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE permission_system;

CREATE TABLE IF NOT EXISTS room_inventory_pool (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_type_id BIGINT NOT NULL COMMENT '房型ID',
    date DATE NOT NULL COMMENT '日期',
    total_rooms INT NOT NULL DEFAULT 0 COMMENT '总房量',
    available_rooms INT NOT NULL DEFAULT 0 COMMENT '可售房量',
    reserved_rooms INT NOT NULL DEFAULT 0 COMMENT '预留房量',
    actual_available INT GENERATED ALWAYS AS (available_rooms - reserved_rooms) STORED COMMENT '实际可售 = 可售 - 预留',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_type_date (room_type_id, date),
    INDEX idx_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房量池';

CREATE TABLE IF NOT EXISTS overbooking_strategy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_type_id BIGINT NOT NULL COMMENT '房型ID',
    enabled TINYINT DEFAULT 0 COMMENT '超售开关：0-关闭，1-启用',
    overbooking_ratio DECIMAL(5,4) DEFAULT 0 COMMENT '超售比例，如0.1表示10%',
    max_overbooking INT DEFAULT 0 COMMENT '最大超售房量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_room_type_id (room_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='超售策略';

CREATE TABLE IF NOT EXISTS overbooking_global_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    enabled TINYINT DEFAULT 0 COMMENT '全局超售开关：0-关闭，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='超售全局配置';

INSERT INTO overbooking_global_config (id, enabled) VALUES (1, 0) ON DUPLICATE KEY UPDATE id=id;

CREATE TABLE IF NOT EXISTS booking_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_type TINYINT NOT NULL COMMENT '规则类型：1-最短入住天数，2-最长入住天数，3-提前预订天数，4-最晚预订时间，5-连住限制，6-房型限制，7-客户类型限制',
    rule_params TEXT NOT NULL COMMENT '规则参数JSON',
    priority INT NOT NULL COMMENT '优先级，数字越小优先级越高',
    enabled TINYINT DEFAULT 1 COMMENT '启用状态：0-禁用，1-启用',
    apply_room_types VARCHAR(500) COMMENT '适用房型ID，逗号分隔，空表示全部',
    apply_date_start DATE COMMENT '适用日期开始',
    apply_date_end DATE COMMENT '适用日期结束',
    apply_sources VARCHAR(200) COMMENT '适用预订来源，逗号分隔，空表示全部',
    description VARCHAR(500) COMMENT '规则描述',
    create_user_id BIGINT COMMENT '创建人ID',
    create_user_name VARCHAR(50) COMMENT '创建人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE INDEX idx_priority (priority),
    INDEX idx_rule_type (rule_type),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预订规则';

CREATE TABLE IF NOT EXISTS booking_rule_validation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT COMMENT '预订单ID',
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_type TINYINT NOT NULL COMMENT '规则类型',
    validation_result TINYINT NOT NULL COMMENT '校验结果：0-通过，1-拦截',
    validation_message VARCHAR(500) COMMENT '校验信息',
    exempted TINYINT DEFAULT 0 COMMENT '是否豁免：0-否，1-是',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_booking_id (booking_id),
    INDEX idx_rule_id (rule_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='规则校验日志';

CREATE TABLE IF NOT EXISTS booking_rule_exemption (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL COMMENT '预订单ID',
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    exemption_reason VARCHAR(500) NOT NULL COMMENT '豁免原因',
    exempted_by_id BIGINT NOT NULL COMMENT '豁免人ID',
    exempted_by_name VARCHAR(50) NOT NULL COMMENT '豁免人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_booking_id (booking_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='规则豁免记录';

CREATE TABLE IF NOT EXISTS channel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    channel_name VARCHAR(100) NOT NULL COMMENT '渠道名称',
    channel_code VARCHAR(50) NOT NULL COMMENT '渠道代码',
    channel_type TINYINT NOT NULL COMMENT '渠道类型：1-OTA平台，2-官方渠道，3-企业协议，4-旅行社，5-其他',
    cooperation_status TINYINT DEFAULT 1 COMMENT '合作状态：0-停用，1-合作中',
    commission_rate DECIMAL(5,4) DEFAULT 0 COMMENT '佣金比例',
    settlement_cycle VARCHAR(50) COMMENT '结算周期',
    contact_person VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE INDEX idx_channel_code (channel_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='渠道';

CREATE TABLE IF NOT EXISTS channel_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    channel_id BIGINT NOT NULL COMMENT '渠道ID',
    room_type_id BIGINT NOT NULL COMMENT '房型ID',
    date DATE NOT NULL COMMENT '日期',
    allocated_rooms INT NOT NULL DEFAULT 0 COMMENT '分配房量',
    used_rooms INT NOT NULL DEFAULT 0 COMMENT '已使用房量',
    remaining_rooms INT GENERATED ALWAYS AS (allocated_rooms - used_rooms) STORED COMMENT '剩余房量',
    share_mode TINYINT DEFAULT 1 COMMENT '共享模式：1-共享，2-独占',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_channel_type_date (channel_id, room_type_id, date),
    INDEX idx_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='渠道房量分配';

CREATE TABLE IF NOT EXISTS channel_price (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    channel_id BIGINT NOT NULL COMMENT '渠道ID',
    room_type_id BIGINT NOT NULL COMMENT '房型ID',
    date DATE NOT NULL COMMENT '日期',
    price_mode TINYINT NOT NULL DEFAULT 1 COMMENT '价格模式：1-跟随门市价，2-固定差价，3-固定折扣，4-独立定价',
    price_value DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '价格值（跟随门市价为0，差价模式为差价金额，折扣模式为折扣率如0.9，独立定价为具体金额）',
    final_price DECIMAL(10,2) COMMENT '最终价格（计算后）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_channel_type_date (channel_id, room_type_id, date),
    INDEX idx_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='渠道价格';

ALTER TABLE booking ADD COLUMN channel_id BIGINT COMMENT '渠道ID' AFTER booking_source;
ALTER TABLE booking ADD COLUMN rule_exempted TINYINT DEFAULT 0 COMMENT '规则豁免：0-否，1-是' AFTER channel_id;
ALTER TABLE booking ADD COLUMN exemption_reason VARCHAR(500) COMMENT '豁免原因' AFTER rule_exempted;
ALTER TABLE booking ADD COLUMN exempted_by_id BIGINT COMMENT '豁免人ID' AFTER exemption_reason;
ALTER TABLE booking ADD COLUMN exempted_by_name VARCHAR(50) COMMENT '豁免人姓名' AFTER exempted_by_id;

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(500, '房量与规则', 0, 4, '/inventory', NULL, NULL, 0, 1, 1, 'Grid'),
(501, '房量池管理', 500, 1, '/inventory/pool', 'booking/InventoryPoolManage', 'inventory:pool:list', 1, 1, 1, 'Calendar'),
(502, '超售策略', 500, 2, '/inventory/overbooking', 'booking/OverbookingStrategy', 'inventory:overbooking:list', 1, 1, 1, 'Warning'),
(503, '房量监控', 500, 3, '/inventory/monitor', 'booking/InventoryMonitor', 'inventory:monitor:list', 1, 1, 1, 'Monitor'),
(504, '预订规则', 500, 4, '/inventory/rules', 'booking/BookingRuleManage', 'inventory:rule:list', 1, 1, 1, 'SetUp');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(511, '房量池查询', 501, 1, '', NULL, 'inventory:pool:query', 2, 1, 1, NULL),
(512, '房量池编辑', 501, 2, '', NULL, 'inventory:pool:edit', 2, 1, 1, NULL),
(513, '房量池批量设置', 501, 3, '', NULL, 'inventory:pool:batch', 2, 1, 1, NULL),
(521, '超售策略查询', 502, 1, '', NULL, 'inventory:overbooking:query', 2, 1, 1, NULL),
(522, '超售策略编辑', 502, 2, '', NULL, 'inventory:overbooking:edit', 2, 1, 1, NULL),
(531, '房量监控查询', 503, 1, '', NULL, 'inventory:monitor:query', 2, 1, 1, NULL),
(532, '房量监控导出', 503, 2, '', NULL, 'inventory:monitor:export', 2, 1, 1, NULL),
(541, '规则查询', 504, 1, '', NULL, 'inventory:rule:query', 2, 1, 1, NULL),
(542, '规则新增', 504, 2, '', NULL, 'inventory:rule:add', 2, 1, 1, NULL),
(543, '规则编辑', 504, 3, '', NULL, 'inventory:rule:edit', 2, 1, 1, NULL),
(544, '规则删除', 504, 4, '', NULL, 'inventory:rule:delete', 2, 1, 1, NULL),
(545, '规则豁免', 504, 5, '', NULL, 'inventory:rule:exempt', 2, 1, 1, NULL);

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(600, '渠道管理', 0, 5, '/channel', NULL, NULL, 0, 1, 1, 'Share'),
(601, '渠道列表', 600, 1, '/channel/list', 'booking/ChannelManage', 'channel:list', 1, 1, 1, 'List'),
(602, '渠道房量', 600, 2, '/channel/inventory', 'booking/ChannelInventory', 'channel:inventory:list', 1, 1, 1, 'Grid'),
(603, '渠道价格', 600, 3, '/channel/price', 'booking/ChannelPrice', 'channel:price:list', 1, 1, 1, 'Money'),
(604, '渠道统计', 600, 4, '/channel/statistics', 'booking/ChannelStats', 'channel:statistics:list', 1, 1, 1, 'DataLine');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(611, '渠道查询', 601, 1, '', NULL, 'channel:query', 2, 1, 1, NULL),
(612, '渠道新增', 601, 2, '', NULL, 'channel:add', 2, 1, 1, NULL),
(613, '渠道编辑', 601, 3, '', NULL, 'channel:edit', 2, 1, 1, NULL),
(614, '渠道删除', 601, 4, '', NULL, 'channel:delete', 2, 1, 1, NULL),
(621, '渠道房量查询', 602, 1, '', NULL, 'channel:inventory:query', 2, 1, 1, NULL),
(622, '渠道房量编辑', 602, 2, '', NULL, 'channel:inventory:edit', 2, 1, 1, NULL),
(631, '渠道价格查询', 603, 1, '', NULL, 'channel:price:query', 2, 1, 1, NULL),
(632, '渠道价格编辑', 603, 2, '', NULL, 'channel:price:edit', 2, 1, 1, NULL),
(641, '渠道统计查询', 604, 1, '', NULL, 'channel:statistics:query', 2, 1, 1, NULL),
(642, '渠道统计导出', 604, 2, '', NULL, 'channel:statistics:export', 2, 1, 1, NULL);

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(700, '高级分析', 0, 6, '/analytics', NULL, NULL, 0, 1, 1, 'TrendCharts'),
(701, '入住率分析', 700, 1, '/analytics/occupancy', 'booking/OccupancyAnalysis', 'analytics:occupancy:list', 1, 1, 1, 'DataAnalysis'),
(702, '预订周期', 700, 2, '/analytics/bookingCycle', 'booking/BookingCycleAnalysis', 'analytics:cycle:list', 1, 1, 1, 'Timer'),
(703, '客户行为', 700, 3, '/analytics/customerBehavior', 'booking/CustomerBehaviorAnalysis', 'analytics:behavior:list', 1, 1, 1, 'User'),
(704, '营收分析', 700, 4, '/analytics/revenue', 'booking/RevenueAnalysis', 'analytics:revenue:list', 1, 1, 1, 'Coin');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(711, '入住率查询', 701, 1, '', NULL, 'analytics:occupancy:query', 2, 1, 1, NULL),
(712, '入住率导出', 701, 2, '', NULL, 'analytics:occupancy:export', 2, 1, 1, NULL),
(721, '预订周期查询', 702, 1, '', NULL, 'analytics:cycle:query', 2, 1, 1, NULL),
(731, '客户行为查询', 703, 1, '', NULL, 'analytics:behavior:query', 2, 1, 1, NULL),
(741, '营收查询', 704, 1, '', NULL, 'analytics:revenue:query', 2, 1, 1, NULL),
(742, '营收导出', 704, 2, '', NULL, 'analytics:revenue:export', 2, 1, 1, NULL);

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(800, '可视化日历', 0, 7, '/visual', NULL, NULL, 0, 1, 1, 'Calendar'),
(801, '房态日历', 800, 1, '/visual/roomStatus', 'booking/RoomStatusCalendar', 'visual:roomStatus:list', 1, 1, 1, 'Calendar'),
(802, '预订甘特图', 800, 2, '/visual/gantt', 'booking/BookingGantt', 'visual:gantt:list', 1, 1, 1, 'Histogram'),
(803, '房量对比', 800, 3, '/visual/inventoryCompare', 'booking/InventoryCompareCalendar', 'visual:compare:list', 1, 1, 1, 'DataLine');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(811, '房态日历查询', 801, 1, '', NULL, 'visual:roomStatus:query', 2, 1, 1, NULL),
(821, '甘特图查询', 802, 1, '', NULL, 'visual:gantt:query', 2, 1, 1, NULL),
(831, '房量对比查询', 803, 1, '', NULL, 'visual:compare:query', 2, 1, 1, NULL);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 500),(1, 501),(1, 502),(1, 503),(1, 504),
(1, 511),(1, 512),(1, 513),(1, 521),(1, 522),(1, 531),(1, 532),
(1, 541),(1, 542),(1, 543),(1, 544),(1, 545),
(1, 600),(1, 601),(1, 602),(1, 603),(1, 604),
(1, 611),(1, 612),(1, 613),(1, 614),(1, 621),(1, 622),(1, 631),(1, 632),(1, 641),(1, 642),
(1, 700),(1, 701),(1, 702),(1, 703),(1, 704),
(1, 711),(1, 712),(1, 721),(1, 731),(1, 741),(1, 742),
(1, 800),(1, 801),(1, 802),(1, 803),
(1, 811),(1, 821),(1, 831);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 500),(3, 501),(3, 502),(3, 503),(3, 504),
(3, 511),(3, 512),(3, 513),(3, 521),(3, 522),(3, 531),(3, 532),
(3, 541),(3, 542),(3, 543),(3, 544),(3, 545),
(3, 600),(3, 601),(3, 602),(3, 603),(3, 604),
(3, 611),(3, 612),(3, 613),(3, 614),(3, 621),(3, 622),(3, 631),(3, 632),(3, 641),(3, 642),
(3, 700),(3, 701),(3, 702),(3, 703),(3, 704),
(3, 711),(3, 712),(3, 721),(3, 731),(3, 741),(3, 742),
(3, 800),(3, 801),(3, 802),(3, 803),
(3, 811),(3, 821),(3, 831);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 500),(4, 501),(4, 502),(4, 503),(4, 504),
(4, 511),(4, 531),
(4, 541),(4, 545),
(4, 600),(4, 601),(4, 602),(4, 603),(4, 604),
(4, 611),(4, 621),(4, 631),(4, 641),
(4, 700),(4, 701),(4, 702),(4, 703),(4, 704),
(4, 711),(4, 721),(4, 731),(4, 741),
(4, 800),(4, 801),(4, 802),(4, 803),
(4, 811),(4, 821),(4, 831);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(6, 500),(6, 501),(6, 503),
(6, 511),(6, 531),
(6, 541),
(6, 600),(6, 601),
(6, 611),
(6, 700),(6, 701),(6, 702),(6, 703),
(6, 711),(6, 721),(6, 731),
(6, 800),(6, 801),(6, 802),
(6, 811),(6, 821);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(7, 500),(7, 503),
(7, 531),(7, 532),
(7, 541),
(7, 600),(7, 601),(7, 604),
(7, 611),(7, 641),(7, 642),
(7, 700),(7, 701),(7, 702),(7, 703),(7, 704),
(7, 711),(7, 712),(7, 721),(7, 731),(7, 741),(7, 742),
(7, 800),(7, 801),(7, 802),(7, 803),
(7, 811),(7, 821),(7, 831);
