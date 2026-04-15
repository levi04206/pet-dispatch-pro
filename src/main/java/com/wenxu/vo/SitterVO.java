package com.wenxu.vo;

import lombok.Data;

@Data
public class SitterVO {

    private Long id;
    private String phone;
    private String realName;
    private String avatar;
    private Integer workStatus;
    private String workStatusDesc;
    private Integer auditStatus;
    private String auditStatusDesc;
    private Integer orderCount;
    private Double rating;
}
