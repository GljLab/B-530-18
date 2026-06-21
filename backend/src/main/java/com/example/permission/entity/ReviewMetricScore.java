package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("review_metric_score")
public class ReviewMetricScore {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long reviewRecordId;

    private Long metricId;

    private String metricName;

    private Integer score;

    private Integer weight;

    private LocalDateTime createTime;
}
