package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("member_point_log")
public class MemberPointLog {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long memberId;

    private String memberNo;

    private Integer pointType;

    private BigDecimal points;

    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;

    private Integer reasonType;

    private String reason;

    private String detail;

    private Integer relatedOrderType;

    private Long relatedOrderId;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime createTime;
}
