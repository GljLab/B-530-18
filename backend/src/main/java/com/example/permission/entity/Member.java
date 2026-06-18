package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("member")
public class Member {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String memberNo;

    private Long customerId;

    private String customerName;

    private String phone;

    private Long levelId;

    private String levelName;

    private Integer registerSource;

    private Long referrerId;

    private String referrerNo;

    private BigDecimal totalPoints;

    private BigDecimal currentPoints;

    private BigDecimal totalSpent;

    private BigDecimal yearlySpent;

    private Integer stayCount;

    private LocalDateTime lastStayTime;

    private Integer status;

    private String freezeReason;

    private LocalDateTime freezeTime;

    private Long freezeOperatorId;

    private String freezeOperatorName;

    private LocalDateTime registerTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;

    @Column(ignore = true)
    private String levelIcon;

    @Column(ignore = true)
    private String levelColor;

    @Column(ignore = true)
    private Customer customer;

    @Column(ignore = true)
    private MemberLevel memberLevel;
}
