package com.wenxu.common;

/**
 * 订单状态机枚举
 */
public enum OrderStatusEnum {

    CANCELED(0, "已取消"),
    PENDING_PAYMENT(1, "待支付"),
    PENDING_ACCEPT(2, "待接单(公海)"),
    ACCEPTED(3, "已接单(前往中)"),
    IN_SERVICE(4, "服务中"),
    COMPLETED(5, "服务完成"),
    EVALUATED(6, "已评价");

    private final Integer status;
    private final String desc;

    OrderStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}