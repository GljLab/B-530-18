package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("review_metric")
public class ReviewMetric {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String metricName;

    private String metricDesc;

    private Integer scoreMin;

    private Integer scoreMax;

    private Integer weight;

    private Integer isRequired;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;
}
