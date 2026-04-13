package com.wenxu.service;

import com.wenxu.dto.OrderCreateDTO;
import com.wenxu.entity.Orders;

import java.util.List;

public interface OrdersService {

    Orders createOrder(OrderCreateDTO orderCreateDTO, Long userId);

    boolean payOrder(String orderSn);

    List<Orders> getPublicPool();

    boolean grabOrder(Long orderId, Long userId);

    boolean startService(Long orderId, String picUrl, Long userId);

    boolean completeService(Long orderId, String picUrl, Long userId);
}
