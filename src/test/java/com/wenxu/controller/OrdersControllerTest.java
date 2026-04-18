package com.wenxu.controller;

import com.wenxu.common.BaseContext;
import com.wenxu.converter.OrderConverter;
import com.wenxu.dto.OrderCreateDTO;
import com.wenxu.entity.Orders;
import com.wenxu.exception.GlobalExceptionHandler;
import com.wenxu.service.OrdersService;
import com.wenxu.vo.OrderVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrdersControllerTest {

    @Mock
    private OrdersService ordersService;

    @Mock
    private OrderConverter orderConverter;

    @InjectMocks
    private OrdersController ordersController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        BaseContext.setCurrentId(100L);
        mockMvc = MockMvcBuilders.standaloneSetup(ordersController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    @Test
    void createOrderShouldUseCurrentUserAndReturnOrderVO() throws Exception {
        Orders order = new Orders();
        order.setId(20L);
        order.setPetId(10L);
        OrderVO vo = new OrderVO();
        vo.setId(20L);
        vo.setPetId(10L);

        when(ordersService.createOrder(any(OrderCreateDTO.class), eq(100L))).thenReturn(order);
        when(orderConverter.toVO(order)).thenReturn(vo);

        mockMvc.perform(post("/api/orders/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"petId\":10,\"reserveTime\":\"2099-04-14T10:00:00\",\"totalAmount\":99.00,\"distance\":3.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(20))
                .andExpect(jsonPath("$.data.petId").value(10));

        ArgumentCaptor<OrderCreateDTO> captor = ArgumentCaptor.forClass(OrderCreateDTO.class);
        verify(ordersService).createOrder(captor.capture(), eq(100L));
        assertEquals(10L, captor.getValue().getPetId());
    }

    @Test
    void createOrderShouldRejectMissingPetId() throws Exception {
        mockMvc.perform(post("/api/orders/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reserveTime\":\"2099-04-14T10:00:00\",\"totalAmount\":99.00,\"distance\":3.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg", containsString("Pet id is required")));
    }

    @Test
    void startServiceShouldAcceptProofBody() throws Exception {
        when(ordersService.startService(20L, "https://example.com/start.jpg", 100L)).thenReturn(true);

        mockMvc.perform(post("/api/orders/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":20,\"proofUrl\":\"https://example.com/start.jpg\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("打卡成功，服务开始"));

        verify(ordersService).startService(20L, "https://example.com/start.jpg", 100L);
    }

    @Test
    void startServiceShouldRejectBlankProofUrl() throws Exception {
        mockMvc.perform(post("/api/orders/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":20,\"proofUrl\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg", containsString("Proof URL cannot be blank")));

        verify(ordersService, never()).startService(any(), any(), eq(100L));
    }

    @Test
    void payOrderShouldUseCurrentUser() throws Exception {
        when(ordersService.payOrder("OD1001", 100L)).thenReturn(true);

        mockMvc.perform(post("/api/orders/pay").param("orderSn", "OD1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("支付成功，订单已进入待接单状态"));

        verify(ordersService).payOrder("OD1001", 100L);
    }

    @Test
    void getPublicPoolShouldUseCurrentUser() throws Exception {
        when(ordersService.getPublicPool(100L)).thenReturn(java.util.List.of());
        when(orderConverter.toVOList(java.util.List.of())).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/orders/publicPool"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(ordersService).getPublicPool(100L);
    }

    @Test
    void listMyOrdersShouldUseCurrentUser() throws Exception {
        Orders order = new Orders();
        order.setId(20L);
        OrderVO vo = new OrderVO();
        vo.setId(20L);

        when(ordersService.listMyOrders(100L)).thenReturn(java.util.List.of(order));
        when(orderConverter.toVOList(java.util.List.of(order))).thenReturn(java.util.List.of(vo));

        mockMvc.perform(get("/api/orders/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0].id").value(20));

        verify(ordersService).listMyOrders(100L);
    }

    @Test
    void listMyServiceOrdersShouldUseCurrentUser() throws Exception {
        Orders order = new Orders();
        order.setId(20L);
        OrderVO vo = new OrderVO();
        vo.setId(20L);

        when(ordersService.listMyServiceOrders(100L)).thenReturn(java.util.List.of(order));
        when(orderConverter.toVOList(java.util.List.of(order))).thenReturn(java.util.List.of(vo));

        mockMvc.perform(get("/api/orders/sitter/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0].id").value(20));

        verify(ordersService).listMyServiceOrders(100L);
    }

    @Test
    void listMyAssignedOrdersShouldUseCurrentUser() throws Exception {
        Orders order = new Orders();
        order.setId(20L);
        OrderVO vo = new OrderVO();
        vo.setId(20L);

        when(ordersService.listMyAssignedOrders(100L)).thenReturn(java.util.List.of(order));
        when(orderConverter.toVOList(java.util.List.of(order))).thenReturn(java.util.List.of(vo));

        mockMvc.perform(get("/api/orders/sitter/assigned"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0].id").value(20));

        verify(ordersService).listMyAssignedOrders(100L);
    }

    @Test
    void cancelOrderShouldUseCurrentUser() throws Exception {
        when(ordersService.cancelOrder(20L, 100L)).thenReturn(true);

        mockMvc.perform(post("/api/orders/cancel").param("orderId", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("订单已取消"));

        verify(ordersService).cancelOrder(20L, 100L);
    }

    @Test
    void cancelOrderShouldReturnBusinessErrorWhenServiceRejects() throws Exception {
        when(ordersService.cancelOrder(20L, 100L)).thenReturn(false);

        mockMvc.perform(post("/api/orders/cancel").param("orderId", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("取消失败，订单不存在、无权操作或当前状态不可取消"));

        verify(ordersService).cancelOrder(20L, 100L);
    }

    @Test
    void grabOrderShouldUseCurrentUser() throws Exception {
        when(ordersService.grabOrder(20L, 100L)).thenReturn(true);

        mockMvc.perform(post("/api/orders/grab").param("orderId", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("抢单成功"));

        verify(ordersService).grabOrder(20L, 100L);
    }

    @Test
    void rejectAssignedOrderShouldUseCurrentUser() throws Exception {
        when(ordersService.rejectAssignedOrder(20L, "档期已满", 100L)).thenReturn(true);

        mockMvc.perform(post("/api/orders/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":20,\"rejectReason\":\"档期已满\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("已拒绝该指定订单"));

        verify(ordersService).rejectAssignedOrder(20L, "档期已满", 100L);
    }

    @Test
    void completeServiceShouldAcceptProofBody() throws Exception {
        when(ordersService.completeService(20L, "https://example.com/end.jpg", 100L)).thenReturn(true);

        mockMvc.perform(post("/api/orders/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":20,\"proofUrl\":\"https://example.com/end.jpg\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("服务已完成"));

        verify(ordersService).completeService(20L, "https://example.com/end.jpg", 100L);
    }

    @Test
    void evaluateOrderShouldAcceptValidBody() throws Exception {
        when(ordersService.evaluateOrder(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(100L))).thenReturn(true);

        mockMvc.perform(post("/api/orders/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":20,\"rating\":5,\"content\":\"服务很好\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("评价成功"));
    }

    @Test
    void evaluateOrderShouldRejectInvalidRating() throws Exception {
        mockMvc.perform(post("/api/orders/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":20,\"rating\":6,\"content\":\"服务很好\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg", containsString("Rating must be between 1 and 5")));
    }

    @Test
    void evaluateOrderShouldRejectMissingOrderId() throws Exception {
        mockMvc.perform(post("/api/orders/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5,\"content\":\"服务很好\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg", containsString("Order id is required")));

        verify(ordersService, never()).evaluateOrder(any(), eq(100L));
    }

    @Test
    void getMyOrderDetailShouldReturnOrderVO() throws Exception {
        Orders order = new Orders();
        order.setId(20L);
        OrderVO vo = new OrderVO();
        vo.setId(20L);

        when(ordersService.getMyOrderDetail(20L, 100L)).thenReturn(order);
        when(orderConverter.toVO(order)).thenReturn(vo);

        mockMvc.perform(get("/api/orders/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(20));
    }

    @Test
    void getMyServiceOrderDetailShouldReturnOrderVO() throws Exception {
        Orders order = new Orders();
        order.setId(20L);
        OrderVO vo = new OrderVO();
        vo.setId(20L);

        when(ordersService.getMyServiceOrderDetail(20L, 100L)).thenReturn(order);
        when(orderConverter.toVO(order)).thenReturn(vo);

        mockMvc.perform(get("/api/orders/sitter/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(20));
    }
}
