package com.wenxu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 宠托师（接单员）表
 */
@Data
@TableName("sitter")
public class Sitter implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;        // 登录手机号
    private String realName;     // 真实姓名
    private String avatar;       // 头像
    private Integer status;      // 接单状态：1可接单 0休息中
    private Integer orderCount;  // 历史接单总数
    private Double rating;       // 综合评分 (如 4.9)

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}