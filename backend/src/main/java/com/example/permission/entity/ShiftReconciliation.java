package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("shift_reconciliation")
public class ShiftReconciliation {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private LocalDate shiftDate;

    private Integer shiftType;

    private Long handoverUserId;

    private String handoverUserName;

    private Long takeoverUserId;

    private String takeoverUserName;

    private BigDecimal cashTotal;

    private BigDecimal cardTotal;

    private BigDecimal mobileTotal;

    private BigDecimal creditTotal;

    private BigDecimal receivableTotal;

    private BigDecimal actualCash;

    private BigDecimal actualCard;

    private BigDecimal actualMobile;

    private BigDecimal actualTotal;

    private BigDecimal difference;

    private String differenceReason;

    private String handoverRemark;

    private String takeoverRemark;

    private Integer takeoverConfirmed;

    private LocalDateTime takeoverConfirmTime;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
