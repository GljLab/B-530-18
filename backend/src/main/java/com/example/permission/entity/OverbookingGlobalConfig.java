package com.example.permission.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("overbooking_global_config")
public class OverbookingGlobalConfig {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Integer enabled;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
