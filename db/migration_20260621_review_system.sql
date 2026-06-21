SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE permission_system;

-- =============================================
-- 评价系统 - 基础配置模块
-- =============================================

-- =============================================
-- 1. 评价指标表
-- =============================================
DROP TABLE IF EXISTS review_metric;
CREATE TABLE review_metric (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  metric_name VARCHAR(50) NOT NULL COMMENT '指标名称',
  metric_desc VARCHAR(500) DEFAULT NULL COMMENT '指标说明',
  score_min INT NOT NULL DEFAULT 1 COMMENT '最低评分',
  score_max INT NOT NULL DEFAULT 5 COMMENT '最高评分',
  weight INT NOT NULL DEFAULT 0 COMMENT '权重百分比(0-100)',
  is_required TINYINT NOT NULL DEFAULT 0 COMMENT '是否必评:0-否,1-是',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '显示排序(数字越小越靠前)',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态:0-禁用,1-启用',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记:0-未删除,1-已删除',
  UNIQUE KEY uk_metric_name (metric_name, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价指标表';

-- =============================================
-- 2. 评价标签表
-- =============================================
DROP TABLE IF EXISTS review_tag;
CREATE TABLE review_tag (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  tag_type TINYINT NOT NULL COMMENT '标签类型:1-好评标签,2-差评标签',
  tag_text VARCHAR(20) NOT NULL COMMENT '标签文本',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '显示排序',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态:0-禁用,1-启用',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记:0-未删除,1-已删除',
  UNIQUE KEY uk_tag_text_type (tag_text, tag_type, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价标签表';

-- =============================================
-- 3. 快捷评语表
-- =============================================
DROP TABLE IF EXISTS review_quick_comment;
CREATE TABLE review_quick_comment (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  comment_content VARCHAR(200) NOT NULL COMMENT '评语内容',
  comment_type TINYINT NOT NULL COMMENT '分类:1-好评评语,2-中性评语,3-差评评语',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '显示排序',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态:0-禁用,1-启用',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记:0-未删除,1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='快捷评语表';

-- =============================================
-- 4. 评价系统菜单 (ID range: 1400-1449)
-- =============================================
-- 父菜单
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1400, '评价管理', 0, 15, NULL, NULL, '', 1, 1, 1, 'ChatDotRound');

-- 评价指标管理
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1401, '评价指标管理', 1400, 1, '/review/metric', 'review/ReviewMetricManage', 'review:metric:list', 1, 1, 1, 'List');

-- 评价标签管理
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1402, '评价标签管理', 1400, 2, '/review/tag', 'review/ReviewTagManage', 'review:tag:list', 1, 1, 1, 'PriceTag');

-- =============================================
-- 5. 按钮权限 - 评价指标
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1410, '指标查询', 1401, 1, '', NULL, 'review:metric:query', 2, 1, 1, NULL),
(1411, '指标新增', 1401, 2, '', NULL, 'review:metric:add', 2, 1, 1, NULL),
(1412, '指标编辑', 1401, 3, '', NULL, 'review:metric:edit', 2, 1, 1, NULL),
(1413, '指标删除', 1401, 4, '', NULL, 'review:metric:delete', 2, 1, 1, NULL),
(1414, '指标状态切换', 1401, 5, '', NULL, 'review:metric:status', 2, 1, 1, NULL);

-- =============================================
-- 6. 按钮权限 - 评价标签
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1420, '标签查询', 1402, 1, '', NULL, 'review:tag:query', 2, 1, 1, NULL),
(1421, '标签新增', 1402, 2, '', NULL, 'review:tag:add', 2, 1, 1, NULL),
(1422, '标签编辑', 1402, 3, '', NULL, 'review:tag:edit', 2, 1, 1, NULL),
(1423, '标签删除', 1402, 4, '', NULL, 'review:tag:delete', 2, 1, 1, NULL),
(1424, '评语新增', 1402, 5, '', NULL, 'review:comment:add', 2, 1, 1, NULL),
(1425, '评语编辑', 1402, 6, '', NULL, 'review:comment:edit', 2, 1, 1, NULL),
(1426, '评语删除', 1402, 7, '', NULL, 'review:comment:delete', 2, 1, 1, NULL);

-- =============================================
-- 7. 分配角色权限 - 超级管理员(role_id=1)
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1400), (1, 1401), (1, 1402),
(1, 1410), (1, 1411), (1, 1412), (1, 1413), (1, 1414),
(1, 1420), (1, 1421), (1, 1422), (1, 1423), (1, 1424), (1, 1425), (1, 1426);

-- =============================================
-- 8. 分配角色权限 - 酒店管理员(role_id=3): 所有配置权限
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1400), (3, 1401), (3, 1402),
(3, 1410), (3, 1411), (3, 1412), (3, 1413), (3, 1414),
(3, 1420), (3, 1421), (3, 1422), (3, 1423), (3, 1424), (3, 1425), (3, 1426);

-- =============================================
-- 9. 分配角色权限 - 前厅部经理(role_id=4): 查看+添加标签和评语
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 1400), (4, 1401), (4, 1402),
(4, 1410),
(4, 1420), (4, 1421), (4, 1422), (4, 1424), (4, 1425);

-- =============================================
-- 10. 分配角色权限 - 客服人员(role_id=12): 查看+添加评语
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(12, 1400), (12, 1401), (12, 1402),
(12, 1410),
(12, 1420), (12, 1424);

-- =============================================
-- 11. 分配角色权限 - 前台员工(role_id=6): 仅查看
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(6, 1400), (6, 1401), (6, 1402),
(6, 1410),
(6, 1420);

-- =============================================
-- 12. 预置测试数据 - 评价指标
-- =============================================
INSERT INTO review_metric (metric_name, metric_desc, score_min, score_max, weight, is_required, sort_order, status, create_time) VALUES
('客房卫生', '评价客房的清洁程度，包括床铺、卫生间、地面等', 1, 5, 20, 1, 1, 1, NOW()),
('服务态度', '评价酒店员工的服务态度和专业程度', 1, 5, 20, 1, 2, 1, NOW()),
('设施设备', '评价酒店设施设备的完好程度和使用体验', 1, 5, 15, 0, 3, 1, NOW()),
('位置交通', '评价酒店地理位置的便利性和交通出行条件', 1, 5, 15, 0, 4, 1, NOW()),
('性价比', '评价酒店价格与服务质量的匹配程度', 1, 5, 15, 1, 5, 1, NOW()),
('早餐质量', '评价酒店早餐的种类、口味和新鲜度', 1, 5, 10, 0, 6, 1, NOW()),
('环境安静', '评价酒店周边环境和房间隔音效果', 1, 5, 5, 0, 7, 1, NOW());

-- =============================================
-- 13. 预置测试数据 - 好评标签
-- =============================================
INSERT INTO review_tag (tag_type, tag_text, sort_order, status, create_time) VALUES
(1, '房间宽敞', 1, 1, NOW()),
(1, '位置便利', 2, 1, NOW()),
(1, '服务周到', 3, 1, NOW()),
(1, '设施完善', 4, 1, NOW()),
(1, '物超所值', 5, 1, NOW()),
(1, '环境安静', 6, 1, NOW()),
(1, '装修精美', 7, 1, NOW()),
(1, '床品舒适', 8, 1, NOW()),
(1, '早餐丰盛', 9, 1, NOW()),
(1, '卫生干净', 10, 1, NOW()),
(1, '停车方便', 11, 1, NOW()),
(1, '前台热情', 12, 1, NOW());

-- =============================================
-- 14. 预置测试数据 - 差评标签
-- =============================================
INSERT INTO review_tag (tag_type, tag_text, sort_order, status, create_time) VALUES
(2, '房间老旧', 1, 1, NOW()),
(2, '卫生不佳', 2, 1, NOW()),
(2, '噪音很大', 3, 1, NOW()),
(2, '服务冷淡', 4, 1, NOW()),
(2, '性价比低', 5, 1, NOW()),
(2, '设施陈旧', 6, 1, NOW()),
(2, '位置偏僻', 7, 1, NOW()),
(2, '空调不好', 8, 1, NOW()),
(2, '水压太小', 9, 1, NOW()),
(2, '早餐一般', 10, 1, NOW()),
(2, '网络不好', 11, 1, NOW()),
(2, '隔音太差', 12, 1, NOW());

-- =============================================
-- 15. 预置测试数据 - 快捷评语
-- =============================================
INSERT INTO review_quick_comment (comment_content, comment_type, sort_order, status, create_time) VALUES
('房间很干净，服务很好，下次还会来', 1, 1, 1, NOW()),
('位置很方便，周边配套齐全，性价比高', 1, 2, 1, NOW()),
('酒店设施完善，前台服务热情，住宿体验很好', 1, 3, 1, NOW()),
('早餐丰盛可口，房间整洁舒适，推荐入住', 1, 4, 1, NOW()),
('交通便利，环境安静，床品舒适，整体满意', 1, 5, 1, NOW()),
('整体还可以，中规中矩，没有特别惊喜', 2, 1, 1, NOW()),
('房间一般，价格合理，适合短期住宿', 2, 2, 1, NOW()),
('设施有点老旧，但服务还不错', 2, 3, 1, NOW()),
('房间有点老旧，需要翻新一下', 3, 1, 1, NOW()),
('隔音效果差，晚上被走廊声音吵到', 3, 2, 1, NOW()),
('卫生状况堪忧，毛巾看起来不干净', 3, 3, 1, NOW()),
('服务态度冷淡，前台工作人员缺乏热情', 3, 4, 1, NOW()),
('性价比太低，这个价位有更好的选择', 3, 5, 1, NOW());
