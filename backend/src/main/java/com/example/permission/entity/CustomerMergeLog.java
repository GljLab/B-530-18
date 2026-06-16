package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("customer_merge_log")
public class CustomerMergeLog {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long sourceCustomerId;

    private Long targetCustomerId;

    private String sourceCustomerName;

    private String targetCustomerName;

    private String mergeDetails;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime mergeTime;
}
