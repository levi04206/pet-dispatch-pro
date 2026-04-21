package com.wenxu.task;

import com.wenxu.service.OrdersService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class OrderTimeoutTask {

    private static final int ORDER_TIMEOUT_MINUTES = 15;

    @Resource
    private OrdersService ordersService;

    /**
     * 每分钟扫描一次超时未支付/未接单订单，自动取消并释放指定宠托师资源。
     */
    @Scheduled(cron = "0 * * * * ?")
    public void cancelTimeoutOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(ORDER_TIMEOUT_MINUTES);
        int canceledCount = ordersService.cancelTimeoutOrders(deadline);
        if (canceledCount > 0) {
            log.info("订单超时自动取消完成，截止时间：{}，取消数量：{}", deadline, canceledCount);
        }
    }
}
