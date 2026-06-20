package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("member_benefit_log")
public class MemberBenefitLog {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long memberId;

    private String memberNo;

    private String memberName;

    private Integer benefitType;

    private String benefitTypeName;

    private Integer relatedOrderType;

    private Long relatedOrderId;

    private String relatedOrderNo;

    private String benefitContent;

    private BigDecimal originalAmount;

    private BigDecimal benefitAmount;

    private BigDecimal actualAmount;

    private String remark;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime createTime;
}
