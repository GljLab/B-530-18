package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("member_level_task_log")
public class MemberLevelTaskLog {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Integer taskType;

    private String taskName;

    private LocalDateTime executeTime;

    private Integer executeResult;

    private Integer processCount;

    private Integer upgradeCount;

    private Integer downgradeCount;

    private Integer resetCount;

    private String errorMsg;

    private Long operatorId;

    private String operatorName;

    private Integer triggerType;

    private LocalDateTime createTime;
}
