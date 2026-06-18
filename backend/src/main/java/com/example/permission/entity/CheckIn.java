package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Table("check_in")
public class CheckIn {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String checkInNo;

    private Long bookingId;

    private String bookingNo;

    private Long customerId;

    private String customerName;

    private String customerPhone;

    private Integer customerType;

    private Integer memberLevel;

    private Long roomTypeId;

    private String roomTypeName;

    private Long roomId;

    private String roomNumber;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private LocalDateTime actualCheckInTime;

    private LocalDateTime actualCheckOutTime;

    private Integer days;

    private Integer stayedDays;

    private BigDecimal roomPrice;

    private BigDecimal roomTotal;

    private Integer extraBedCount;

    private BigDecimal extraBedPrice;

    private BigDecimal extraBedTotal;

    private BigDecimal otherFee;

    private BigDecimal discount;

    private BigDecimal totalAmount;

    private BigDecimal paidAmount;

    private BigDecimal payableAmount;

    private BigDecimal depositAmount;

    private Integer depositMethod;

    private String depositVoucherNo;

    private Long depositAuthorizedBy;

    private String depositAuthorizedName;

    private Integer keyCardCount;

    private Integer keyCardReturned;

    private Integer guestCount;

    private String specialRequirements;

    private Integer bookingSource;

    private Integer status;

    private Integer isOverdue;

    private Long operatorId;

    private String operatorName;

    private Long checkOutOperatorId;

    private String checkOutOperatorName;

    private String remark;

    private Long agreementUnitId;

    private String agreementUnitName;

    private Integer guaranteeType;

    private Integer isCredit;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;

    @Column(ignore = true)
    private List<CheckInGuest> guests;

    @Column(ignore = true)
    private List<KeyCardRecord> keyCards;

    @Column(ignore = true)
    private List<ConsumptionRecord> consumptions;

    @Column(ignore = true)
    private List<CheckInOperationLog> operationLogs;

    @Column(ignore = true)
    private Room room;

    @Column(ignore = true)
    private RoomType roomType;

    @Column(ignore = true)
    private Customer customer;
}
