package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("invoice")
public class Invoice {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String invoiceNo;

    private Integer relatedOrderType;

    private Long relatedOrderId;

    private String relatedOrderNo;

    private Long customerId;

    private String customerName;

    private Integer invoiceType;

    private Integer titleType;

    private String invoiceTitle;

    private String taxNo;

    private String companyAddress;

    private String bankAccount;

    private BigDecimal invoiceAmount;

    private BigDecimal orderAmount;

    private Integer invoiceItem;

    private String contactPhone;

    private String contactEmail;

    private String invoiceCode;

    private String invoiceNumber;

    private String invoicePdf;

    private Integer isElectronic;

    private Integer emailSent;

    private String mailRecipient;

    private String mailAddress;

    private String mailCompany;

    private String mailTrackingNo;

    private LocalDateTime mailTime;

    private Long applicantId;

    private String applicantName;

    private LocalDateTime applyTime;

    private Long processorId;

    private String processorName;

    private LocalDateTime processTime;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;
}
