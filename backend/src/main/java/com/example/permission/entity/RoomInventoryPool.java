package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("room_inventory_pool")
public class RoomInventoryPool {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long roomTypeId;

    private LocalDate date;

    private Integer totalRooms;

    private Integer availableRooms;

    private Integer reservedRooms;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
