package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("customer_blacklist")
public class CustomerBlacklist {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long customerId;

    private Integer reason;

    private String detailDescription;

    private String evidenceMaterials;

    private Integer blacklistType;

    private LocalDateTime expireTime;

    private Integer status;

    private Long applicantId;

    private String applicantName;

    private LocalDateTime applyTime;

    private Long approverId;

    private String approverName;

    private LocalDateTime approveTime;

    private String approveOpinion;

    private String rejectReason;

    private String removeReason;

    private Long removeApplicantId;

    private String removeApplicantName;

    private LocalDateTime removeApplyTime;

    private Long removeApproverId;

    private String removeApproverName;

    private LocalDateTime removeApproveTime;

    private String removeApproveOpinion;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Column(ignore = true)
    private String customerName;

    @Column(ignore = true)
    private String customerPhone;

    @Column(ignore = true)
    private String customerIdNumber;
}
