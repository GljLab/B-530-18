package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("check_in_guest")
public class CheckInGuest {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long checkInId;

    private String checkInNo;

    private Integer isMain;

    private String name;

    private Integer gender;

    private Integer idType;

    private String idNumber;

    private LocalDate idExpiryDate;

    private String idPhotoFront;

    private String idPhotoBack;

    private String phone;

    private String nationality;

    private String address;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
