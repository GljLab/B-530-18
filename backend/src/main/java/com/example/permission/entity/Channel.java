package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("channel")
public class Channel {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String channelName;

    private String channelCode;

    private Integer channelType;

    private Integer cooperationStatus;

    private BigDecimal commissionRate;

    private String settlementCycle;

    private String contactPerson;

    private String contactPhone;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;
}
