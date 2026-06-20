SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE permission_system;

ALTER TABLE booking
    ADD COLUMN member_id BIGINT COMMENT '会员ID' AFTER customer_phone,
    ADD COLUMN member_no VARCHAR(30) COMMENT '会员卡号' AFTER member_id,
    ADD COLUMN points_used DECIMAL(12,2) DEFAULT 0 COMMENT '使用积分数量' AFTER member_no,
    ADD COLUMN points_deduction_amount DECIMAL(12,2) DEFAULT 0 COMMENT '积分抵扣金额' AFTER points_used;

ALTER TABLE check_in
    ADD COLUMN member_id BIGINT COMMENT '会员ID' AFTER customer_phone,
    ADD COLUMN member_no VARCHAR(30) COMMENT '会员卡号' AFTER member_id,
    ADD COLUMN earned_points DECIMAL(12,2) DEFAULT 0 COMMENT '获得积分' AFTER member_no;
