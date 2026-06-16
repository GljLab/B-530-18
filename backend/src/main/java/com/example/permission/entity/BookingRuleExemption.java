package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("booking_rule_exemption")
public class BookingRuleExemption {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long bookingId;

    private Long ruleId;

    private String ruleName;

    private String exemptionReason;

    private Long exemptedById;

    private String exemptedByName;

    private LocalDateTime createTime;
}
