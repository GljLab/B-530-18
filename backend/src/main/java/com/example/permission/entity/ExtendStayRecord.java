package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("extend_stay_record")
public class ExtendStayRecord {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String extendNo;

    private Long checkInId;

    private String checkInNo;

    private Long roomId;

    private String roomNumber;

    private LocalDate originalCheckOutDate;

    private LocalDate newCheckOutDate;

    private Integer extendDays;

    private BigDecimal roomPrice;

    private BigDecimal extendAmount;

    private String reason;

    private LocalDateTime extendTime;

    private Long operatorId;

    private String operatorName;

    private String remark;

    private LocalDateTime createTime;
}
