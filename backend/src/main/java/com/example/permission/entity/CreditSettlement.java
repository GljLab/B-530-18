package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("credit_settlement")
public class CreditSettlement {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String settlementNo;

    private Long agreementUnitId;

    private String agreementUnitName;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private Integer billCount;

    private BigDecimal totalAmount;

    private Integer settlementMethod;

    private LocalDate settlementDate;

    private String voucherNo;

    private String invoiceNo;

    private Long operatorId;

    private String operatorName;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
