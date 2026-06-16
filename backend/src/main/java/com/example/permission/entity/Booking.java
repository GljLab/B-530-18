package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("booking")
public class Booking {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String bookingNo;

    private Long customerId;

    private String customerName;

    private String customerPhone;

    private Long roomTypeId;

    private String roomTypeName;

    private Long roomId;

    private String roomNumber;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private Integer days;

    private BigDecimal roomPrice;

    private BigDecimal roomTotal;

    private Integer extraBedCount;

    private BigDecimal extraBedPrice;

    private BigDecimal extraBedTotal;

    private BigDecimal discount;

    private BigDecimal otherFee;

    private BigDecimal totalAmount;

    private BigDecimal paidAmount;

    private Integer guestCount;

    private String guestNames;

    private String guestPhone;

    private String bookingSource;

    private String sourceRemark;

    private String guaranteeType;

    private LocalDateTime expectedArrivalTime;

    private String specialRequirements;

    private Integer status;

    private String cancelReason;

    private String cancelDetail;

    private BigDecimal cancelPenalty;

    private LocalDateTime cancelTime;

    private Long cancelOperatorId;

    private String cancelOperatorName;

    private Long createUserId;

    private String createUserName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;
}
