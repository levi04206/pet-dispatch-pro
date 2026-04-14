package com.wenxu.controller;

import com.wenxu.common.ApiMessages;
import com.wenxu.common.BaseContext;
import com.wenxu.common.Result;
import com.wenxu.converter.OrderConverter;
import com.wenxu.dto.OrderCreateDTO;
import com.wenxu.dto.OrderEvaluateDTO;
import com.wenxu.dto.OrderProofDTO;
import com.wenxu.entity.Orders;
import com.wenxu.service.OrdersService;
import com.wenxu.vo.OrderVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        Long userId = BaseContext.getCurrentId();
        boolean paid = ordersService.payOrder(orderSn, userId);
        return paid ? Result.success(ApiMessages.ORDER_PAY_SUCCESS) : Result.error(ApiMessages.ORDER_PAY_FAILED);
    }

    @GetMapping("/publicPool")
    public Result<List<OrderVO>> getPublicPool() {
        Long userId = BaseContext.getCurrentId();
        return Result.success(orderConverter.toVOList(ordersService.getPublicPool(userId)));
    }

    @GetMapping("/my")
    public Result<List<OrderVO>> listMyOrders() {
        Long userId = BaseContext.getCurrentId();
        return Result.success(orderConverter.toVOList(ordersService.listMyOrders(userId)));
    }

    @GetMapping("/{orderId}")
    public Result<OrderVO> getMyOrderDetail(@PathVariable Long orderId) {
        Long userId = BaseContext.getCurrentId();
        Orders orders = ordersService.getMyOrderDetail(orderId, userId);
        if (orders == null) {
            return Result.error(ApiMessages.ORDER_NOT_FOUND_OR_FORBIDDEN);
        }
        return Result.success(orderConverter.toVO(orders));
    }

    @GetMapping("/sitter/my")
    public Result<List<OrderVO>> listMyServiceOrders() {
        Long userId = BaseContext.getCurrentId();
        return Result.success(orderConverter.toVOList(ordersService.listMyServiceOrders(userId)));
    }

    @GetMapping("/sitter/{orderId}")
    public Result<OrderVO> getMyServiceOrderDetail(@PathVariable Long orderId) {
        Long userId = BaseContext.getCurrentId();
        Orders orders = ordersService.getMyServiceOrderDetail(orderId, userId);
        if (orders == null) {
            return Result.error(ApiMessages.ORDER_NOT_FOUND_OR_FORBIDDEN);
        }
        return Result.success(orderConverter.toVO(orders));
    }

    @PostMapping("/cancel")
    public Result<String> cancelOrder(@RequestParam Long orderId) {
        Long userId = BaseContext.getCurrentId();
        boolean canceled = ordersService.cancelOrder(orderId, userId);
        return canceled ? Result.success(ApiMessages.ORDER_CANCEL_SUCCESS) : Result.error(ApiMessages.ORDER_CANCEL_FAILED);
    }

    @PostMapping("/evaluate")
    public Result<String> evaluateOrder(@Valid @RequestBody OrderEvaluateDTO orderEvaluateDTO) {
        Long userId = BaseContext.getCurrentId();
        boolean evaluated = ordersService.evaluateOrder(orderEvaluateDTO, userId);
        return evaluated ? Result.success(ApiMessages.ORDER_EVALUATE_SUCCESS) : Result.error(ApiMessages.ORDER_EVALUATE_FAILED);
    }

    @PostMapping("/grab")
    public Result<String> grabOrder(@RequestParam Long orderId) {
        Long userId = BaseContext.getCurrentId();
        boolean grabbed = ordersService.grabOrder(orderId, userId);
        return grabbed ? Result.success(ApiMessages.ORDER_GRAB_SUCCESS) : Result.error(ApiMessages.ORDER_GRAB_FAILED);
    }

    @PostMapping("/start")
    public Result<String> startService(@Valid @RequestBody OrderProofDTO orderProofDTO) {
        Long userId = BaseContext.getCurrentId();
        boolean started = ordersService.startService(orderProofDTO.getOrderId(), orderProofDTO.getProofUrl(), userId);
        return started ? Result.success(ApiMessages.ORDER_START_SUCCESS) : Result.error(ApiMessages.ORDER_OPERATION_FAILED);
    }

    @PostMapping("/complete")
    public Result<String> completeService(@Valid @RequestBody OrderProofDTO orderProofDTO) {
        Long userId = BaseContext.getCurrentId();
        boolean completed = ordersService.completeService(orderProofDTO.getOrderId(), orderProofDTO.getProofUrl(), userId);
        return completed ? Result.success(ApiMessages.ORDER_SERVICE_COMPLETE_SUCCESS) : Result.error(ApiMessages.ORDER_OPERATION_FAILED);
    }
}
