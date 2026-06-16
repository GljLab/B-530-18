package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("booking_rule")
public class BookingRule {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String ruleName;

    private Integer ruleType;

    private String ruleParams;

    private Integer priority;

    private Integer enabled;

    private String applyRoomTypes;

    private LocalDate applyDateStart;

    private LocalDate applyDateEnd;

    private String applySources;

    private String description;

    private Long createUserId;

    private String createUserName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;
}
