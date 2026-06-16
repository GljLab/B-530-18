package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("customer_preference")
public class CustomerPreference {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long customerId;

    private String preferredRoomType;

    private String preferredFloor;

    private String preferredOrientation;

    private String preferredBedType;

    private String preferredView;

    private String specialNeeds;

    private String servicePreference;

    private Integer dietVegetarian;

    private Integer dietHalal;

    private Integer dietSeafoodAllergy;

    private Integer dietNoSpicy;

    private String dietOtherAllergy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
