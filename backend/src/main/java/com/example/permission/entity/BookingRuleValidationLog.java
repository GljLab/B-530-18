package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("booking_rule_validation_log")
public class BookingRuleValidationLog {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long bookingId;

    private Long ruleId;

    private String ruleName;

    private Integer ruleType;

    private Integer validationResult;

    private String validationMessage;

    private Integer exempted;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime createTime;
}
