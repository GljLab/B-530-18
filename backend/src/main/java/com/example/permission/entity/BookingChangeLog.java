package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("booking_change_log")
public class BookingChangeLog {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long bookingId;

    private String bookingNo;

    private String fieldName;

    private String oldValue;

    private String newValue;

    private String changeType;

    private String remark;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime createTime;
}
