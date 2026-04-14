package com.wenxu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 核心调度订单表
 */
@Data
@TableName("orders")
public class Orders implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderSn;
    private Long userId;
    private Long petId;
    private Long sitterId;

    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private BigDecimal distance;

    private Integer status;

    @Version
    private Integer version;

    private String startProof;
    private String endProof;
    private Integer evaluateRating;
    private String evaluateContent;

    private LocalDateTime reserveTime;
    private LocalDateTime payTime;
    private LocalDateTime acceptTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime evaluateTime;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
