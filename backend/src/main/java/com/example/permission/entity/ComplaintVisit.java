package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("complaint_visit")
public class ComplaintVisit {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long complaintId;

    private LocalDateTime visitTime;

    private Integer visitMethod;

    private Integer satisfaction;

    private String visitRemark;

    private Long visitUserId;

    private String visitUserName;

    private Integer needReprocess;

    private LocalDateTime createTime;

    private Integer deleted;
}
