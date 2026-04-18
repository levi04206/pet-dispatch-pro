package com.wenxu.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {

    private Long id;
    private String orderSn;
    private Long petId;
    private Long sitterId;
    private Long targetSitterId;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private BigDecimal distance;
    private Integer status;
    private String statusDesc;
    private Integer evaluateRating;
    private String evaluateContent;
    private String rejectReason;
    private LocalDateTime reserveTime;
    private LocalDateTime payTime;
    private LocalDateTime acceptTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime rejectTime;
    private LocalDateTime evaluateTime;
}
