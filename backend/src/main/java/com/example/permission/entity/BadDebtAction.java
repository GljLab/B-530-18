package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("bad_debt_action")
public class BadDebtAction {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long badDebtId;

    private Integer actionType;

    private LocalDateTime actionTime;

    private Integer actionMethod;

    private String actionResult;

    private BigDecimal recoveredAmount;

    private Integer paymentMethod;

    private LocalDateTime paymentTime;

    private Long operatorId;

    private String operatorName;

    private String remark;

    private LocalDateTime createTime;
}
