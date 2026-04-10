package com.wenxu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
    private Long userId;         // 🚨 新增：关联的用户ID (极其重要)
    private String realName;     // 真实姓名
    private String avatar;       // 头像
    private String idCard;
    // 接单大厅状态
    @TableField("work_status")   // 填数据库真实的列名
    private Integer workStatus;  // 接单状态：0休息中 1接单中 2服务中
    // 入驻审核状态
    private Integer auditStatus; // 审核状态：0待审核 1通过 2审核驳回
    private Integer orderCount;  // 历史接单总数
    private Double rating;       // 综合评分 (如 4.9)

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}