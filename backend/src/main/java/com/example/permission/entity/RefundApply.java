package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("refund_apply")
public class RefundApply {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String refundNo;

    private Integer relatedOrderType;

    private Long relatedOrderId;

    private String relatedOrderNo;

    private Long customerId;

    private String customerName;

    private String customerPhone;

    private Integer refundType;

    private BigDecimal refundAmount;

    private BigDecimal approvedAmount;

    private String refundReason;

    private String proofMaterials;

    private Long applicantId;

    private String applicantName;

    private LocalDateTime applyTime;

    private Long approverId;

    private String approverName;

    private LocalDateTime approveTime;

    private String approveRemark;

    private String rejectReason;

    private Integer refundMethod;

    private String refundVoucherNo;

    private LocalDateTime refundTime;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
