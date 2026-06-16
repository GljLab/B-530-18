package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("overbooking_strategy")
public class OverbookingStrategy {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long roomTypeId;

    private Integer enabled;

    private BigDecimal overbookingRatio;

    private Integer maxOverbooking;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
