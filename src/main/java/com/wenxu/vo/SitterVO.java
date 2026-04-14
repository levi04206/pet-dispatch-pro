package com.wenxu.vo;

import lombok.Data;

@Data
public class SitterVO {

    private Long id;
    private String phone;
    private String realName;
    private String avatar;
    private Integer workStatus;
    private Integer auditStatus;
    private Integer orderCount;
    private Double rating;
}
