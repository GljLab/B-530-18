package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Table("review_record")
public class ReviewRecord {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long checkInId;

    private String checkInNo;

    private Long invitationId;

    private Long customerId;

    private String customerName;

    private String customerPhone;

    private Long memberId;

    private String memberNo;

    private Long roomTypeId;

    private String roomTypeName;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private BigDecimal overallScore;

    private String reviewContent;

    private String selectedTags;

    private Integer isAnonymous;

    private Integer reviewStatus;

    private LocalDateTime auditTime;

    private Long auditorId;

    private String auditorName;

    private String auditRemark;

    private Integer isTop;

    private LocalDateTime topTime;

    private String replyContent;

    private LocalDateTime replyTime;

    private Long repliedBy;

    private String repliedName;

    private Integer likeCount;

    private LocalDateTime reviewTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;

    @Column(ignore = true)
    private List<ReviewMetricScore> metricScores;

    @Column(ignore = true)
    private List<ReviewImage> images;
}
