package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("customer_address")
public class CustomerAddress {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long customerId;

    private Integer addressType;

    private String province;

    private String city;

    private String district;

    private String detail;

    private String postalCode;

    private Integer isDefault;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;
}
