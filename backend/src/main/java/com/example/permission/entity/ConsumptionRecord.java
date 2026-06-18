package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("consumption_record")
public class ConsumptionRecord {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String consumptionNo;

    private Long checkInId;

    private String checkInNo;

    private Long roomId;

    private String roomNumber;

    private Long customerId;

    private String customerName;

    private Integer consumptionType;

    private String itemName;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalAmount;

    private LocalDateTime consumptionTime;

    private Integer billingMethod;

    private Integer isSettled;

    private LocalDateTime settleTime;

    private Long operatorId;

    private String operatorName;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
