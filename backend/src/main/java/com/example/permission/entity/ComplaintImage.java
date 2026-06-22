package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("complaint_image")
public class ComplaintImage {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long complaintId;

    private String imageUrl;

    private String imageName;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private Integer deleted;
}
