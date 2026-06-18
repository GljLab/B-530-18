package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("credit_bill")
public class CreditBill {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String billNo;

    private Long agreementUnitId;

    private String agreementUnitName;

    private Long checkInId;

    private String checkInNo;

    private String customerName;

    private String roomNumber;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private BigDecimal roomFee;

    private BigDecimal extraFee;

    private BigDecimal discountAmount;

    private BigDecimal totalAmount;

    private LocalDateTime billTime;

    private Long settlementId;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
