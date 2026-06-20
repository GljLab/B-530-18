package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("point_earn_rule")
public class PointEarnRule {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String ruleName;

    private Integer ruleType;

    private BigDecimal pointValue;

    private BigDecimal pointAmount;

    private BigDecimal pointRate;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;
}
