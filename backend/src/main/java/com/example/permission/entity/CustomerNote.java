package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("customer_note")
public class CustomerNote {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long customerId;

    private String content;

    private Integer importance;

    private Integer isPinned;

    private String attachments;

    private String mentionRoles;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;
}
