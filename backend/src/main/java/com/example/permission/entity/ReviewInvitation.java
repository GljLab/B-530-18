package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("review_invitation")
public class ReviewInvitation {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long checkInId;

    private String checkInNo;

    private Long customerId;

    private String customerName;

    private String customerPhone;

    private Long roomTypeId;

    private String roomTypeName;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private String reviewCode;

    private String reviewLink;

    private Integer reviewStatus;

    private LocalDateTime reviewTime;

    private Integer isSent;

    private LocalDateTime sendTime;

    private Integer sendMethod;

    private LocalDateTime expireTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;
}
