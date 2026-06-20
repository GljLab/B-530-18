package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Table("point_consume_rule")
public class PointConsumeRule {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String ruleName;

    private Integer ruleType;

    private BigDecimal exchangePoints;

    private BigDecimal exchangeAmount;

    private BigDecimal maxPointsPerUse;

    private BigDecimal minOrderAmount;

    private BigDecimal deductionCap;

    private String applicableLevels;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;

    @Column(ignore = true)
    private List<Long> applicableLevelList;
}
