package com.wenxu.controller;

import com.wenxu.annotation.Idempotent;
import com.wenxu.annotation.LogOperation;
import com.wenxu.common.ApiMessages;
import com.wenxu.common.BaseContext;
import com.wenxu.common.Result;
import com.wenxu.converter.OrderConverter;
import com.wenxu.dto.OrderCreateDTO;
import com.wenxu.dto.OrderEvaluateDTO;
import com.wenxu.dto.OrderProofDTO;
import com.wenxu.dto.OrderRejectDTO;
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

    /**
     * 用户创建订单，订单初始状态为待支付。
     */
    @PostMapping("/create")
    @Idempotent(expireTime = 5)
    @LogOperation(module = "订单模块", action = "创建订单")
    public Result<OrderVO> createOrder(@Valid @RequestBody OrderCreateDTO orderCreateDTO) {
        Long userId = BaseContext.getCurrentId();
        Orders orders = ordersService.createOrder(orderCreateDTO, userId);
        return Result.success(orderConverter.toVO(orders));
    }

    /**
     * 用户模拟支付订单，支付后订单进入公共待接单池。
     */
    @PostMapping("/pay")
    @Idempotent(expireTime = 5)
    @LogOperation(module = "订单模块", action = "支付订单")
    public Result<String> payOrder(@RequestParam String orderSn) {
        Long userId = BaseContext.getCurrentId();
        boolean paid = ordersService.payOrder(orderSn, userId);
        return paid ? Result.success(ApiMessages.ORDER_PAY_SUCCESS) : Result.error(ApiMessages.ORDER_PAY_FAILED);
    }

    /**
     * 宠托师查看公共订单池。
     */
    @GetMapping("/publicPool")
    public Result<List<OrderVO>> getPublicPool() {
        Long userId = BaseContext.getCurrentId();
        return Result.success(orderConverter.toVOList(ordersService.getPublicPool(userId)));
    }

    /**
     * 用户查看自己创建的订单列表。
     */
    @GetMapping("/my")
    public Result<List<OrderVO>> listMyOrders() {
        Long userId = BaseContext.getCurrentId();
        return Result.success(orderConverter.toVOList(ordersService.listMyOrders(userId)));
    }

    /**
     * 用户查看自己的订单详情。
     */
    @GetMapping("/{orderId}")
    public Result<OrderVO> getMyOrderDetail(@PathVariable Long orderId) {
        Long userId = BaseContext.getCurrentId();
        Orders orders = ordersService.getMyOrderDetail(orderId, userId);
        if (orders == null) {
            return Result.error(ApiMessages.ORDER_NOT_FOUND_OR_FORBIDDEN);
        }
        return Result.success(orderConverter.toVO(orders));
    }

    /**
     * 宠托师查看自己承接的订单列表。
     */
    @GetMapping("/sitter/my")
    public Result<List<OrderVO>> listMyServiceOrders() {
        Long userId = BaseContext.getCurrentId();
        return Result.success(orderConverter.toVOList(ordersService.listMyServiceOrders(userId)));
    }

    /**
     * 宠托师查看用户指定给自己的待接单订单。
     */
    @GetMapping("/sitter/assigned")
    public Result<List<OrderVO>> listMyAssignedOrders() {
        Long userId = BaseContext.getCurrentId();
        return Result.success(orderConverter.toVOList(ordersService.listMyAssignedOrders(userId)));
    }

    /**
     * 宠托师查看自己承接的订单详情。
     */
    @GetMapping("/sitter/{orderId}")
    public Result<OrderVO> getMyServiceOrderDetail(@PathVariable Long orderId) {
        Long userId = BaseContext.getCurrentId();
        Orders orders = ordersService.getMyServiceOrderDetail(orderId, userId);
        if (orders == null) {
            return Result.error(ApiMessages.ORDER_NOT_FOUND_OR_FORBIDDEN);
        }
        return Result.success(orderConverter.toVO(orders));
    }

    /**
     * 用户取消待支付或待接单订单。
     */
    @PostMapping("/cancel")
    @Idempotent(expireTime = 5)
    @LogOperation(module = "订单模块", action = "取消订单")
    public Result<String> cancelOrder(@RequestParam Long orderId) {
        Long userId = BaseContext.getCurrentId();
        boolean canceled = ordersService.cancelOrder(orderId, userId);
        return canceled ? Result.success(ApiMessages.ORDER_CANCEL_SUCCESS) : Result.error(ApiMessages.ORDER_CANCEL_FAILED);
    }

    /**
     * 用户评价已完成服务的订单。
     */
    @PostMapping("/evaluate")
    @Idempotent(expireTime = 5)
    @LogOperation(module = "订单模块", action = "评价订单")
    public Result<String> evaluateOrder(@Valid @RequestBody OrderEvaluateDTO orderEvaluateDTO) {
        Long userId = BaseContext.getCurrentId();
        boolean evaluated = ordersService.evaluateOrder(orderEvaluateDTO, userId);
        return evaluated ? Result.success(ApiMessages.ORDER_EVALUATE_SUCCESS) : Result.error(ApiMessages.ORDER_EVALUATE_FAILED);
    }

    /**
     * 宠托师抢单，将公共订单绑定到当前宠托师。
     */
    @PostMapping("/grab")
    @Idempotent(expireTime = 5)
    @LogOperation(module = "订单模块", action = "抢单")
    public Result<String> grabOrder(@RequestParam Long orderId) {
        Long userId = BaseContext.getCurrentId();
        boolean grabbed = ordersService.grabOrder(orderId, userId);
        return grabbed ? Result.success(ApiMessages.ORDER_GRAB_SUCCESS) : Result.error(ApiMessages.ORDER_GRAB_FAILED);
    }

    /**
     * 宠托师拒绝用户指定给自己的订单。
     */
    @PostMapping("/reject")
    @Idempotent(expireTime = 5)
    @LogOperation(module = "订单模块", action = "宠托师拒单")
    public Result<String> rejectAssignedOrder(@Valid @RequestBody OrderRejectDTO orderRejectDTO) {
        Long userId = BaseContext.getCurrentId();
        boolean rejected = ordersService.rejectAssignedOrder(orderRejectDTO.getOrderId(), orderRejectDTO.getRejectReason(), userId);
        return rejected ? Result.success(ApiMessages.ORDER_REJECT_SUCCESS) : Result.error(ApiMessages.ORDER_REJECT_FAILED);
    }

    /**
     * 宠托师开始服务，并记录开始履约凭证。
     */
    @PostMapping("/start")
    @Idempotent(expireTime = 5)
    @LogOperation(module = "订单模块", action = "开始服务")
    public Result<String> startService(@Valid @RequestBody OrderProofDTO orderProofDTO) {
        Long userId = BaseContext.getCurrentId();
        boolean started = ordersService.startService(orderProofDTO.getOrderId(), orderProofDTO.getProofUrl(), userId);
        return started ? Result.success(ApiMessages.ORDER_START_SUCCESS) : Result.error(ApiMessages.ORDER_OPERATION_FAILED);
    }

    /**
     * 宠托师完成服务，并记录完成履约凭证。
     */
    @PostMapping("/complete")
    @Idempotent(expireTime = 5)
    @LogOperation(module = "订单模块", action = "完成服务")
    public Result<String> completeService(@Valid @RequestBody OrderProofDTO orderProofDTO) {
        Long userId = BaseContext.getCurrentId();
        boolean completed = ordersService.completeService(orderProofDTO.getOrderId(), orderProofDTO.getProofUrl(), userId);
        return completed ? Result.success(ApiMessages.ORDER_SERVICE_COMPLETE_SUCCESS) : Result.error(ApiMessages.ORDER_OPERATION_FAILED);
    }
}
