package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("check_out_record")
public class CheckOutRecord {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String checkOutNo;

    private Long checkInId;

    private String checkInNo;

    private Long customerId;

    private String customerName;

    private Long roomId;

    private String roomNumber;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private LocalDateTime actualCheckOutTime;

    private Integer stayedDays;

    private BigDecimal roomTotal;

    private BigDecimal extraBedTotal;

    private BigDecimal otherFee;

    private BigDecimal damageCompensation;

    private BigDecimal discount;

    private BigDecimal totalAmount;

    private BigDecimal paidAmount;

    private BigDecimal depositAmount;

    private BigDecimal depositDeducted;

    private BigDecimal depositRefund;

    private BigDecimal additionalPayment;

    private BigDecimal refundAmount;

    private BigDecimal payableAmount;

    private Integer paymentMethod;

    private String paymentVoucherNo;

    private Integer depositMethod;

    private Integer keyCardReturned;

    private Integer keyCardLost;

    private Integer roomChecked;

    private Integer roomCheckResult;

    private String damageItems;

    private String damageDescription;

    private String damagePhotos;

    private Integer status;

    private Long operatorId;

    private String operatorName;

    private String remark;

    private LocalDateTime createTime;
}
