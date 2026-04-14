package com.wenxu.controller;

import com.wenxu.common.BaseContext;
import com.wenxu.common.Result;
import com.wenxu.converter.OrderConverter;
import com.wenxu.dto.OrderCreateDTO;
import com.wenxu.entity.Orders;
import com.wenxu.service.OrdersService;
import com.wenxu.vo.OrderVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    @Resource
    private OrdersService ordersService;

    @Resource
    private OrderConverter orderConverter;

    @PostMapping("/create")
    public Result<OrderVO> createOrder(@Valid @RequestBody OrderCreateDTO orderCreateDTO) {
        Long userId = BaseContext.getCurrentId();
        Orders orders = ordersService.createOrder(orderCreateDTO, userId);
        return Result.success(orderConverter.toVO(orders));
    }

    @PostMapping("/pay")
    public Result<String> payOrder(@RequestParam String orderSn) {
        boolean paid = ordersService.payOrder(orderSn);
        return paid ? Result.success("支付成功，订单已进入待接单状态") : Result.error("订单不存在或状态异常，无法支付");
    }

    @GetMapping("/publicPool")
    public Result<List<OrderVO>> getPublicPool() {
        return Result.success(orderConverter.toVOList(ordersService.getPublicPool()));
    }

    @PostMapping("/grab")
    public Result<String> grabOrder(@RequestParam Long orderId) {
        Long userId = BaseContext.getCurrentId();
        boolean grabbed = ordersService.grabOrder(orderId, userId);
        return grabbed ? Result.success("抢单成功") : Result.error("抢单失败，订单已被抢走或当前用户不具备接单资格");
    }

    @PostMapping("/start")
    public Result<String> startService(@RequestParam Long orderId, @RequestParam String picUrl) {
        Long userId = BaseContext.getCurrentId();
        boolean started = ordersService.startService(orderId, picUrl, userId);
        return started ? Result.success("打卡成功，服务开始") : Result.error("操作失败，请检查订单状态或归属权");
    }

    @PostMapping("/complete")
    public Result<String> completeService(@RequestParam Long orderId, @RequestParam String picUrl) {
        Long userId = BaseContext.getCurrentId();
        boolean completed = ordersService.completeService(orderId, picUrl, userId);
        return completed ? Result.success("服务已完成") : Result.error("操作失败，请检查订单状态或归属权");
    }
}
