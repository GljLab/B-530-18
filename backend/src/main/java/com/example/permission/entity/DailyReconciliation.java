package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("daily_reconciliation")
public class DailyReconciliation {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private LocalDate reconcileDate;

    private BigDecimal cashRoom;

    private BigDecimal cashDeposit;

    private BigDecimal cashOther;

    private BigDecimal cashTotal;

    private BigDecimal cardRoom;

    private BigDecimal cardDeposit;

    private BigDecimal cardOther;

    private BigDecimal cardTotal;

    private BigDecimal mobileAlipay;

    private BigDecimal mobileWechat;

    private BigDecimal mobileTotal;

    private BigDecimal creditAmount;

    private BigDecimal prepaidAmount;

    private BigDecimal refundAmount;

    private BigDecimal receivableTotal;

    private BigDecimal actualCash;

    private BigDecimal actualCard;

    private BigDecimal actualMobile;

    private BigDecimal actualTotal;

    private BigDecimal difference;

    private String differenceReason;

    private String differenceProof;

    private Long operatorId;

    private String operatorName;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
