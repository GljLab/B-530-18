package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("agreement_unit")
public class AgreementUnit {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String unitName;

    private String creditCode;

    private String address;

    private String contactPhone;

    private String contactPerson;

    private String contactPosition;

    private String contactMobile;

    private String contactEmail;

    private Integer cooperationType;

    private LocalDate signDate;

    private LocalDate validUntil;

    private Integer settlementCycle;

    private BigDecimal creditLimit;

    private BigDecimal currentDebt;

    private BigDecimal discountRate;

    private String agreementFile;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;
}
