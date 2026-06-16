package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("channel_inventory")
public class ChannelInventory {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long channelId;

    private Long roomTypeId;

    private LocalDate date;

    private Integer allocatedRooms;

    private Integer usedRooms;

    private Integer shareMode;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
