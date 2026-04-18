package com.wenxu.service;

import com.wenxu.dto.OrderCreateDTO;
import com.wenxu.dto.OrderEvaluateDTO;
import com.wenxu.entity.Orders;

import java.util.List;

public interface OrdersService {

    Orders createOrder(OrderCreateDTO orderCreateDTO, Long userId);

    boolean payOrder(String orderSn, Long userId);

    List<Orders> getPublicPool(Long userId);

    List<Orders> listMyOrders(Long userId);

    List<Orders> listMyServiceOrders(Long userId);

    List<Orders> listMyAssignedOrders(Long userId);

    Orders getMyOrderDetail(Long orderId, Long userId);

    Orders getMyServiceOrderDetail(Long orderId, Long userId);

    boolean cancelOrder(Long orderId, Long userId);

    boolean evaluateOrder(OrderEvaluateDTO orderEvaluateDTO, Long userId);

    boolean grabOrder(Long orderId, Long userId);

    boolean rejectAssignedOrder(Long orderId, String rejectReason, Long userId);

    boolean startService(Long orderId, String picUrl, Long userId);

    boolean completeService(Long orderId, String picUrl, Long userId);
}
