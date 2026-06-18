package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("check_in_operation_log")
public class CheckInOperationLog {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long checkInId;

    private String checkInNo;

    private Integer operationType;

    private String operationDesc;

    private String detail;

    private Long operatorId;

    private String operatorName;

    private String operatorRole;

    private LocalDateTime createTime;
}
