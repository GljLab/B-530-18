package com.example.permission.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("customer_tag_relation")
public class CustomerTagRelation {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long customerId;

    private Long tagId;

    private LocalDateTime createTime;

    @Column(ignore = true)
    private String tagName;
}
