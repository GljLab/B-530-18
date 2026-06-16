package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("channel_price")
public class ChannelPrice {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long channelId;

    private Long roomTypeId;

    private LocalDate date;

    private Integer priceMode;

    private BigDecimal priceValue;

    private BigDecimal finalPrice;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
