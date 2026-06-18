package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("bad_debt")
public class BadDebt {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String debtNo;

    private Integer relatedOrderType;

    private Long relatedOrderId;

    private String relatedOrderNo;

    private Long customerId;

    private String customerName;

    private Integer debtType;

    private BigDecimal originalAmount;

    private BigDecimal recoveredAmount;

    private BigDecimal remainingAmount;

    private LocalDateTime debtTime;

    private Integer overdueDays;

    private Integer status;

    private String writeOffReason;

    private String writeOffProof;

    private Long writeOffApproverId;

    private String writeOffApproverName;

    private LocalDateTime writeOffApproveTime;

    private Integer legalStatus;

    private String legalInfo;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;
}
