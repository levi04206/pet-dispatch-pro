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
    private Integer isNeutered; // 是否绝育：0否 1是
    private Integer aggressiveTag; // 攻击性标签：0温顺 1轻微敏感 2高攻击风险

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
