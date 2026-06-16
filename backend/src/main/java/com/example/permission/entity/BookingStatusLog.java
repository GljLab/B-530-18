package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("booking_status_log")
public class BookingStatusLog {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long bookingId;

    private String bookingNo;

    private Integer oldStatus;

    private Integer newStatus;

    private String remark;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime createTime;
}
