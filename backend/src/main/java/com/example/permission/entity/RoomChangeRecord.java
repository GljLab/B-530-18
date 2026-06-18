package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("room_change_record")
public class RoomChangeRecord {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String changeNo;

    private Long checkInId;

    private String checkInNo;

    private Long oldRoomId;

    private String oldRoomNumber;

    private Long oldRoomTypeId;

    private String oldRoomTypeName;

    private BigDecimal oldRoomPrice;

    private Long newRoomId;

    private String newRoomNumber;

    private Long newRoomTypeId;

    private String newRoomTypeName;

    private BigDecimal newRoomPrice;

    private BigDecimal priceDiff;

    private Integer changeReason;

    private String changeDetail;

    private LocalDateTime changeTime;

    private Long operatorId;

    private String operatorName;

    private String remark;

    private LocalDateTime createTime;
}
