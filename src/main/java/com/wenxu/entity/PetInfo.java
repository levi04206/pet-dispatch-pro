package com.wenxu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 宠物档案表
 */
@Data
@TableName("pet_info")
public class PetInfo implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;      // 归属用户ID
    @TableField("name") // 强制映射到数据库的 name 字段
    private String petName;   // 宠物昵称

    @TableField("type")
    private Integer petType;  // 宠物类型：1猫 2狗 3其他
    private String breed;     // 品种 (如：金毛、布偶)
    private Double weight;    // 体重(kg)
//    private String healthStatus; // 健康状况描述

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}