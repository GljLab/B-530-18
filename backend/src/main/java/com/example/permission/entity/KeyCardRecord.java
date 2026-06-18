package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("key_card_record")
public class KeyCardRecord {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long checkInId;

    private String checkInNo;

    private Long roomId;

    private String roomNumber;

    private String cardNo;

    private Integer cardType;

    private LocalDateTime issueTime;

    private LocalDateTime expireTime;

    private LocalDateTime returnTime;

    private Integer status;

    private Long operatorId;

    private String operatorName;

    private Long returnOperatorId;

    private String returnOperatorName;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
