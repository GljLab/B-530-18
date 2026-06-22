package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Table("complaint")
public class Complaint {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String complaintNo;

    private Long checkInId;

    private String checkInNo;

    private Long customerId;

    private String customerName;

    private String customerPhone;

    private String customerEmail;

    private Long roomTypeId;

    private String roomTypeName;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private Integer complaintType;

    private String complaintContent;

    private String expectedSolution;

    private Integer complaintStatus;

    private String verifyCode;

    private Long acceptUserId;

    private String acceptUserName;

    private LocalDateTime acceptTime;

    private String acceptRemark;

    private Long assignUserId;

    private String assignUserName;

    private Integer rejectReason;

    private String rejectRemark;

    private Long rejectUserId;

    private String rejectUserName;

    private LocalDateTime rejectTime;

    private String handleSolution;

    private Integer handleResult;

    private String compensationPlan;

    private String handleRemark;

    private Long handleUserId;

    private String handleUserName;

    private LocalDateTime handleTime;

    private Integer needReprocess;

    private LocalDateTime complaintTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;

    @Column(ignore = true)
    private List<ComplaintImage> images;

    @Column(ignore = true)
    private List<ComplaintVisit> visits;
}
