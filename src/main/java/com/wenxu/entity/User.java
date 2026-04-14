package com.wenxu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * C端用户表
 */
@Data
@TableName("user")
public class User implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;     // 手机号
    private String nickname;  // 用户昵称
    private String avatar;    // 头像URL
    private Integer status;   // 账号状态: 1正常 0封禁
    private String role;      // 用户角色: USER普通用户 SITTER宠托师 ADMIN管理员

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
