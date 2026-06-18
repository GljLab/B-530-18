package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Table("member_level")
public class MemberLevel {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String levelName;

    private String levelCode;

    private String levelIcon;

    private String levelColor;

    private Integer sortOrder;

    private Integer upgradeType;

    private BigDecimal upgradeCondition;

    private BigDecimal keepCondition;

    private BigDecimal roomDiscount;

    private BigDecimal diningDiscount;

    private BigDecimal pointRate;

    private BigDecimal depositReduction;

    private String services;

    private String otherBenefits;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;

    @Column(ignore = true)
    private List<String> serviceList;

    @Column(ignore = true)
    private List<String> otherBenefitList;

    @Column(ignore = true)
    private Long memberCount;
}
