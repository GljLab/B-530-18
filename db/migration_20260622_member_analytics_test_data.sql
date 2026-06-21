SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE permission_system;

-- =============================================
-- 会员深度分析模块 - 测试数据
-- =============================================
-- 注意：运行前请确保已有会员等级、客户、房型、入住单等基础数据
-- 本脚本补充生成各等级会员样本、不同消费频次样本、不同流失风险样本、权益使用记录样本

-- =============================================
-- 1. 确保有足够的客户数据
-- =============================================
-- 补充客户数据（如果不够）
-- 这里假设已有基础客户数据，如需要可自行补充

-- =============================================
-- 2. 更新会员表数据 - 增加更多维度差异
-- =============================================
-- 注意：以下SQL用于更新现有会员数据，使其具有不同的消费特征
-- 实际使用时请根据现有数据调整

-- 更新不同等级会员的消费数据（模拟不同等级的消费能力差异）
-- 普通会员：低消费、低频次
UPDATE member m
SET 
  total_spent = FLOOR(RAND() * 800) + 50,
  current_points = FLOOR((FLOOR(RAND() * 800) + 50) * 1),
  total_points = FLOOR((FLOOR(RAND() * 800) + 50) * 1.2),
  stay_count = FLOOR(RAND() * 3) + 1,
  yearly_spent = FLOOR(RAND() * 500) + 30,
  last_stay_time = DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 90) DAY)
WHERE m.level_id = 1 AND m.deleted = 0;

-- 银卡会员：中低消费、中频
UPDATE member m
SET 
  total_spent = FLOOR(RAND() * 3000) + 1000,
  current_points = FLOOR((FLOOR(RAND() * 3000) + 1000) * 1.2),
  total_points = FLOOR((FLOOR(RAND() * 3000) + 1000) * 1.5),
  stay_count = FLOOR(RAND() * 5) + 2,
  yearly_spent = FLOOR(RAND() * 2000) + 500,
  last_stay_time = DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 60) DAY)
WHERE m.level_id = 2 AND m.deleted = 0;

-- 金卡会员：中高消费、高频
UPDATE member m
SET 
  total_spent = FLOOR(RAND() * 10000) + 5000,
  current_points = FLOOR((FLOOR(RAND() * 10000) + 5000) * 1.5),
  total_points = FLOOR((FLOOR(RAND() * 10000) + 5000) * 2),
  stay_count = FLOOR(RAND() * 10) + 5,
  yearly_spent = FLOOR(RAND() * 5000) + 2000,
  last_stay_time = DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY)
WHERE m.level_id = 3 AND m.deleted = 0;

-- 钻石会员：高消费、超高频
UPDATE member m
SET 
  total_spent = FLOOR(RAND() * 30000) + 20000,
  current_points = FLOOR((FLOOR(RAND() * 30000) + 20000) * 2),
  total_points = FLOOR((FLOOR(RAND() * 30000) + 20000) * 2.5),
  stay_count = FLOOR(RAND() * 20) + 10,
  yearly_spent = FLOOR(RAND() * 15000) + 10000,
  last_stay_time = DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 15) DAY)
WHERE m.level_id = 4 AND m.deleted = 0;

-- =============================================
-- 3. 生成不同流失风险的会员
-- =============================================
-- 低风险（3-6个月未入住）- 从普通和银卡中选取
UPDATE member m
SET last_stay_time = DATE_SUB(NOW(), INTERVAL FLOOR(90 + (RAND() * 90)) DAY)
WHERE m.level_id IN (1, 2) AND m.deleted = 0
ORDER BY RAND()
LIMIT 20;

-- 中风险（6-12个月未入住）- 从各等级中选取
UPDATE member m
SET last_stay_time = DATE_SUB(NOW(), INTERVAL FLOOR(180 + (RAND() * 180)) DAY)
WHERE m.deleted = 0
ORDER BY RAND()
LIMIT 15;

-- 高风险（12个月以上未入住）
UPDATE member m
SET last_stay_time = DATE_SUB(NOW(), INTERVAL FLOOR(365 + (RAND() * 180)) DAY)
WHERE m.deleted = 0
ORDER BY RAND()
LIMIT 10;

-- =============================================
-- 4. 补充等级变更记录（如果数据不足）
-- =============================================
-- 生成近12个月的等级变更记录
-- 注意：这只是模拟数据，实际使用请根据业务情况调整

-- 生成升级记录
INSERT INTO member_level_log (member_id, member_no, change_type, old_level_id, old_level_name, new_level_id, new_level_name, reason, operator_id, operator_name, create_time)
SELECT 
  m.id,
  m.member_no,
  1,
  m.level_id - 1,
  CASE m.level_id - 1 
    WHEN 1 THEN '普通会员'
    WHEN 2 THEN '银卡会员'
    WHEN 3 THEN '金卡会员'
    ELSE '普通会员'
  END,
  m.level_id,
  m.level_name,
  '消费达标自动升级',
  1,
  '系统',
  DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY)
FROM member m
WHERE m.level_id > 1 AND m.deleted = 0
ORDER BY RAND()
LIMIT 30;

-- 生成降级记录
INSERT INTO member_level_log (member_id, member_no, change_type, old_level_id, old_level_name, new_level_id, new_level_name, reason, operator_id, operator_name, create_time)
SELECT 
  m.id,
  m.member_no,
  2,
  m.level_id + 1,
  CASE m.level_id + 1 
    WHEN 2 THEN '银卡会员'
    WHEN 3 THEN '金卡会员'
    WHEN 4 THEN '钻石会员'
    ELSE '金卡会员'
  END,
  m.level_id,
  m.level_name,
  '年度保级失败自动降级',
  1,
  '系统',
  DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY)
FROM member m
WHERE m.level_id < 4 AND m.deleted = 0
ORDER BY RAND()
LIMIT 20;

-- =============================================
-- 5. 生成权益使用记录
-- =============================================
-- 折扣优惠权益
INSERT INTO member_benefit_log (member_id, member_no, benefit_type, benefit_type_name, related_order_type, related_order_id, benefit_amount, create_time)
SELECT 
  m.id,
  m.member_no,
  1,
  '房费折扣',
  1,
  FLOOR(RAND() * 100) + 1,
  FLOOR(RAND() * 200) + 20,
  DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 180) DAY)
FROM member m
WHERE m.level_id > 1 AND m.deleted = 0
ORDER BY RAND()
LIMIT 50;

-- 免费升级权益
INSERT INTO member_benefit_log (member_id, member_no, benefit_type, benefit_type_name, related_order_type, related_order_id, benefit_amount, create_time)
SELECT 
  m.id,
  m.member_no,
  2,
  '免费升级房型',
  1,
  FLOOR(RAND() * 100) + 1,
  FLOOR(RAND() * 300) + 100,
  DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 180) DAY)
FROM member m
WHERE m.level_id >= 3 AND m.deleted = 0
ORDER BY RAND()
LIMIT 30;

-- 延迟退房权益
INSERT INTO member_benefit_log (member_id, member_no, benefit_type, benefit_type_name, related_order_type, related_order_id, benefit_amount, create_time)
SELECT 
  m.id,
  m.member_no,
  4,
  '延迟退房',
  1,
  FLOOR(RAND() * 100) + 1,
  0,
  DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 180) DAY)
FROM member m
WHERE m.level_id >= 2 AND m.deleted = 0
ORDER BY RAND()
LIMIT 40;

-- 押金减免权益
INSERT INTO member_benefit_log (member_id, member_no, benefit_type, benefit_type_name, related_order_type, related_order_id, benefit_amount, create_time)
SELECT 
  m.id,
  m.member_no,
  3,
  '押金减免',
  1,
  FLOOR(RAND() * 100) + 1,
  FLOOR(RAND() * 500) + 100,
  DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 180) DAY)
FROM member m
WHERE m.level_id >= 2 AND m.deleted = 0
ORDER BY RAND()
LIMIT 35;

-- 积分加倍权益
INSERT INTO member_benefit_log (member_id, member_no, benefit_type, benefit_type_name, related_order_type, related_order_id, benefit_amount, create_time)
SELECT 
  m.id,
  m.member_no,
  5,
  '积分发放',
  1,
  FLOOR(RAND() * 100) + 1,
  FLOOR(RAND() * 100) + 10,
  DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 180) DAY)
FROM member m
WHERE m.level_id > 1 AND m.deleted = 0
ORDER BY RAND()
LIMIT 45;

-- =============================================
-- 6. 高价值会员标记（消费排名前10%）
-- =============================================
-- 数据已在member表中，分析时按total_spent排序即可

-- =============================================
-- 验证数据
-- =============================================
-- 查看各等级会员数
-- SELECT level_name, COUNT(*) as count FROM member WHERE deleted = 0 GROUP BY level_id;

-- 查看流失风险分布
-- SELECT 
--   CASE 
--     WHEN last_stay_time IS NULL THEN '从未入住'
--     WHEN DATEDIFF(NOW(), last_stay_time) <= 90 THEN '活跃'
--     WHEN DATEDIFF(NOW(), last_stay_time) <= 180 THEN '低风险'
--     WHEN DATEDIFF(NOW(), last_stay_time) <= 365 THEN '中风险'
--     ELSE '高风险'
--   END as risk_level,
--   COUNT(*) as count
-- FROM member
-- WHERE deleted = 0
-- GROUP BY risk_level;

-- 查看权益使用统计
-- SELECT benefit_name, COUNT(*) as use_count, SUM(benefit_amount) as total_amount
-- FROM member_benefit_log
-- GROUP BY benefit_type;
