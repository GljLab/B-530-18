package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("booking_detail")
public class BookingDetail {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long bookingId;

    private String bookingNo;

    private LocalDate stayDate;

    private BigDecimal price;

    private String dayType;

    private LocalDateTime createTime;
}
