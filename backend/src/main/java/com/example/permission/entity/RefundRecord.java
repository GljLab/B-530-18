package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("refund_record")
public class RefundRecord {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String refundNo;

    private Long bookingId;

    private String bookingNo;

    private Long customerId;

    private String customerName;

    private BigDecimal totalAmount;

    private BigDecimal paidAmount;

    private BigDecimal refundableAmount;

    private BigDecimal deductionAmount;

    private BigDecimal actualRefundAmount;

    private String refundReason;

    private String refundMethod;

    private Integer status;

    private Long applicantId;

    private String applicantName;

    private LocalDateTime createTime;

    private Long approverId;

    private String approverName;

    private LocalDateTime approveTime;

    private String approveRemark;

    private LocalDateTime refundTime;

    private LocalDateTime updateTime;
}
