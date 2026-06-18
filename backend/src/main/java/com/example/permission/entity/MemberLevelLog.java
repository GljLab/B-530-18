package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("member_level_log")
public class MemberLevelLog {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long memberId;

    private String memberNo;

    private Integer changeType;

    private Long oldLevelId;

    private String oldLevelName;

    private Long newLevelId;

    private String newLevelName;

    private String reason;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime createTime;
}
