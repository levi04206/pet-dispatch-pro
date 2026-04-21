package com.wenxu.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLogVO {

    private Long id;

    private Long userId;

    private String role;

    private String module;

    private String action;

    private String requestPath;

    private String ip;

    private Long costTimeMs;

    private LocalDateTime createTime;

    private Boolean success;

    private String resultText;
}
