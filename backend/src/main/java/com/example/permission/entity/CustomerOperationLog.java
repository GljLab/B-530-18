package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("customer_operation_log")
public class CustomerOperationLog {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long customerId;

    private String customerName;

    private Integer operationType;

    private Long operatorId;

    private String operatorName;

    private String remark;

    private String changeField;

    private String oldValue;

    private String newValue;

    private String operatorIp;

    private LocalDateTime createTime;
}
