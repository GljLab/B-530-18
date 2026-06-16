package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("customer")
public class Customer {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String name;

    private Integer gender;

    private LocalDate birthDate;

    private String nationality;

    private String avatar;

    private Integer idType;

    private String idNumber;

    private LocalDate idExpiryDate;

    private String idPhotoFront;

    private String idPhotoBack;

    private String phone;

    private String backupPhone;

    private String email;

    private String wechat;

    private String emergencyContactName;

    private String emergencyContactRelation;

    private String emergencyContactPhone;

    private Integer customerType;

    private Integer customerSource;

    private Long referrerId;

    private Integer importance;

    private Integer status;

    private LocalDateTime firstOrderTime;

    private Integer totalOrders;

    private BigDecimal totalSpent;

    private LocalDateTime lastOrderTime;

    private String freezeReason;

    private LocalDateTime freezeTime;

    private Long freezeOperatorId;

    private String freezeOperatorName;

    private String unfreezeReason;

    private LocalDateTime unfreezeTime;

    private Long unfreezeOperatorId;

    private String unfreezeOperatorName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;

    @Column(ignore = true)
    private java.util.List<CustomerAddress> addresses;

    @Column(ignore = true)
    private String referrerName;

    @Column(ignore = true)
    private BigDecimal avgSpent;

    @Column(ignore = true)
    private Long lifecycleDays;

    @Column(ignore = true)
    private java.util.List<CustomerTag> tags;

    @Column(ignore = true)
    private Boolean hasImportantNote;
}
