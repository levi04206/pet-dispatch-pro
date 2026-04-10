package com.wenxu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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

    private String orderSn;      // 订单流水号
    private Long userId;         // 下单用户ID
    private Long petId;          // 宠物档案ID
    private Long sitterId;       // 承接宠托师ID

    private BigDecimal totalAmount; // 订单总金额
    private BigDecimal payAmount;   // 实付金额
    private BigDecimal distance;    // 服务距离(km)

    private Integer status;      // 状态: 1待支付 2待接单 3已接单 4服务中 5服务完成 6已评价 0已取消
    private String startProof;   // 开始服务凭证
    private String endProof;     // 结束服务凭证

    private LocalDateTime reserveTime; // 预约上门时间
    private LocalDateTime payTime;     // 支付时间
    private LocalDateTime acceptTime;  // 接单时间
    private LocalDateTime startTime;   // 开始服务时间
    private LocalDateTime endTime;     // 结束时间

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}