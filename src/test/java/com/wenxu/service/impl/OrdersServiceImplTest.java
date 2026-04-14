package com.wenxu.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wenxu.common.OrderStatusEnum;
import com.wenxu.converter.OrderConverter;
import com.wenxu.dto.OrderCreateDTO;
import com.wenxu.entity.Orders;
import com.wenxu.entity.Sitter;
import com.wenxu.mapper.OrdersMapper;
import com.wenxu.mapper.PetInfoMapper;
import com.wenxu.mapper.SitterMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdersServiceImplTest {

    @Mock
    private OrdersMapper ordersMapper;

    @Mock
    private SitterMapper sitterMapper;

    @Mock
    private PetInfoMapper petInfoMapper;

    @Mock
    private OrderConverter orderConverter;

    @InjectMocks
    private OrdersServiceImpl ordersService;

    @Test
    void createOrderShouldFillSystemFields() {
        OrderCreateDTO dto = new OrderCreateDTO();
        Orders mappedOrder = new Orders();
        mappedOrder.setPetId(1L);

        when(petInfoMapper.selectCount(any())).thenReturn(1L);
        when(orderConverter.toEntity(dto)).thenReturn(mappedOrder);
        when(ordersMapper.insert(mappedOrder)).thenReturn(1);

        Orders result = ordersService.createOrder(dto, 100L);

        assertEquals(100L, result.getUserId());
        assertNotNull(result.getOrderSn());
        assertTrue(result.getOrderSn().startsWith("OD"));
        assertEquals(OrderStatusEnum.PENDING_PAYMENT.getStatus(), result.getStatus());
        assertEquals(0, result.getVersion());
        assertEquals(new BigDecimal("99.00"), result.getTotalAmount());
        assertEquals(new BigDecimal("99.00"), result.getPayAmount());
        assertEquals(new BigDecimal("3.5"), result.getDistance());
        verify(ordersMapper).insert(mappedOrder);
    }

    @Test
    void createOrderShouldRejectPetNotOwnedByCurrentUser() {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setPetId(1L);

        when(petInfoMapper.selectCount(any())).thenReturn(0L);

        assertThrows(IllegalArgumentException.class, () -> ordersService.createOrder(dto, 100L));
        verify(orderConverter, never()).toEntity(any());
        verify(ordersMapper, never()).insert(any());
    }

    @Test
    void payOrderShouldMovePendingPaymentToPendingAccept() {
        Orders order = new Orders();
        order.setStatus(OrderStatusEnum.PENDING_PAYMENT.getStatus());

        when(ordersMapper.selectOne(any())).thenReturn(order);
        when(ordersMapper.updateById(order)).thenReturn(1);

        boolean paid = ordersService.payOrder("OD1001");

        assertTrue(paid);
        assertEquals(OrderStatusEnum.PENDING_ACCEPT.getStatus(), order.getStatus());
        assertNotNull(order.getPayTime());
        verify(ordersMapper).updateById(order);
    }

    @Test
    void listMyOrdersShouldScopeQueryByCurrentUser() {
        Orders order = new Orders();
        order.setId(20L);
        when(ordersMapper.selectList(any())).thenReturn(List.of(order));

        List<Orders> orders = ordersService.listMyOrders(100L);

        assertEquals(List.of(order), orders);
        verify(ordersMapper).selectList(any());
    }

    @Test
    void listMyServiceOrdersShouldScopeQueryByApprovedSitter() {
        Sitter sitter = new Sitter();
        sitter.setId(10L);
        sitter.setAuditStatus(1);
        Orders order = new Orders();
        order.setId(20L);

        when(sitterMapper.selectOne(any())).thenReturn(sitter);
        when(ordersMapper.selectList(any())).thenReturn(List.of(order));

        List<Orders> orders = ordersService.listMyServiceOrders(100L);

        assertEquals(List.of(order), orders);
        verify(ordersMapper).selectList(any());
    }

    @Test
    void listMyServiceOrdersShouldReturnEmptyWhenCurrentUserIsNotApprovedSitter() {
        Sitter sitter = new Sitter();
        sitter.setId(10L);
        sitter.setAuditStatus(0);

        when(sitterMapper.selectOne(any())).thenReturn(sitter);

        List<Orders> orders = ordersService.listMyServiceOrders(100L);

        assertTrue(orders.isEmpty());
        verify(ordersMapper, never()).selectList(any());
    }

    @Test
    void grabOrderShouldBindApprovedAcceptingSitter() {
        Sitter sitter = new Sitter();
        sitter.setId(10L);
        sitter.setAuditStatus(1);
        sitter.setWorkStatus(1);

        when(sitterMapper.selectOne(any())).thenReturn(sitter);
        when(ordersMapper.update(any(), any())).thenReturn(1);

        boolean grabbed = ordersService.grabOrder(20L, 100L);

        assertTrue(grabbed);
        verify(ordersMapper).update(any(), any());
    }

    @Test
    void grabOrderShouldRejectUnapprovedSitter() {
        Sitter sitter = new Sitter();
        sitter.setId(10L);
        sitter.setAuditStatus(0);
        sitter.setWorkStatus(1);

        when(sitterMapper.selectOne(any())).thenReturn(sitter);

        boolean grabbed = ordersService.grabOrder(20L, 100L);

        assertFalse(grabbed);
        verify(ordersMapper, never()).update(any(), any());
    }

    @Test
    void startServiceShouldBindSitterIdDerivedFromCurrentUser() {
        Sitter sitter = new Sitter();
        sitter.setId(10L);
        sitter.setAuditStatus(1);

        when(sitterMapper.selectOne(any())).thenReturn(sitter);
        when(ordersMapper.update(any(), any())).thenReturn(1);

        boolean started = ordersService.startService(20L, "start-proof.jpg", 100L);

        assertTrue(started);
        LambdaUpdateWrapper<Orders> wrapper = captureOrderUpdateWrapper();
        Assertions.assertAll(
                () -> assertTrue(wrapper.getSqlSegment().contains("id")),
                () -> assertTrue(wrapper.getSqlSegment().contains("sitter_id")),
                () -> assertTrue(wrapper.getSqlSegment().contains("status")),
                () -> assertTrue(wrapper.getSqlSet().contains("start_proof")),
                () -> assertTrue(wrapper.getSqlSet().contains("version = version + 1"))
        );
    }

    @Test
    void startServiceShouldRejectWhenCurrentUserHasNoSitterProfile() {
        when(sitterMapper.selectOne(any())).thenReturn(null);

        boolean started = ordersService.startService(20L, "start-proof.jpg", 100L);

        assertFalse(started);
        verify(ordersMapper, never()).update(any(), any());
    }

    @Test
    void completeServiceShouldBindSitterIdDerivedFromCurrentUser() {
        Sitter sitter = new Sitter();
        sitter.setId(10L);
        sitter.setAuditStatus(1);

        when(sitterMapper.selectOne(any())).thenReturn(sitter);
        when(ordersMapper.update(any(), any())).thenReturn(1);

        boolean completed = ordersService.completeService(20L, "end-proof.jpg", 100L);

        assertTrue(completed);
        LambdaUpdateWrapper<Orders> wrapper = captureOrderUpdateWrapper();
        Assertions.assertAll(
                () -> assertTrue(wrapper.getSqlSegment().contains("id")),
                () -> assertTrue(wrapper.getSqlSegment().contains("sitter_id")),
                () -> assertTrue(wrapper.getSqlSegment().contains("status")),
                () -> assertTrue(wrapper.getSqlSet().contains("end_proof")),
                () -> assertTrue(wrapper.getSqlSet().contains("version = version + 1"))
        );
    }

    @Test
    void completeServiceShouldRejectWhenCurrentUserHasNoSitterProfile() {
        when(sitterMapper.selectOne(any())).thenReturn(null);

        boolean completed = ordersService.completeService(20L, "end-proof.jpg", 100L);

        assertFalse(completed);
        verify(ordersMapper, never()).update(any(), any());
    }

    @Test
    void startServiceShouldRejectUnapprovedSitter() {
        Sitter sitter = new Sitter();
        sitter.setId(10L);
        sitter.setAuditStatus(0);

        when(sitterMapper.selectOne(any())).thenReturn(sitter);

        boolean started = ordersService.startService(20L, "start-proof.jpg", 100L);

        assertFalse(started);
        verify(ordersMapper, never()).update(any(), any());
    }

    @Test
    void completeServiceShouldRejectUnapprovedSitter() {
        Sitter sitter = new Sitter();
        sitter.setId(10L);
        sitter.setAuditStatus(0);

        when(sitterMapper.selectOne(any())).thenReturn(sitter);

        boolean completed = ordersService.completeService(20L, "end-proof.jpg", 100L);

        assertFalse(completed);
        verify(ordersMapper, never()).update(any(), any());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private LambdaUpdateWrapper<Orders> captureOrderUpdateWrapper() {
        ArgumentCaptor<LambdaUpdateWrapper> captor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(ordersMapper).update(any(), captor.capture());
        return captor.getValue();
    }
}
