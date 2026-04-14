package com.wenxu.controller;

import com.wenxu.common.BaseContext;
import com.wenxu.converter.OrderConverter;
import com.wenxu.entity.Orders;
import com.wenxu.exception.GlobalExceptionHandler;
import com.wenxu.service.OrdersService;
import com.wenxu.vo.OrderVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
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
