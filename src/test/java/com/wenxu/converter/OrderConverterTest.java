package com.wenxu.converter;

import com.wenxu.common.OrderStatusEnum;
import com.wenxu.entity.Orders;
import com.wenxu.vo.OrderVO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrderConverterTest {

    private final OrderConverter orderConverter = Mappers.getMapper(OrderConverter.class);

    @Test
    void toVOShouldFillStatusDesc() {
        Orders order = new Orders();
        order.setId(20L);
        order.setStatus(OrderStatusEnum.PENDING_ACCEPT.getStatus());

        OrderVO vo = orderConverter.toVO(order);

        assertEquals(20L, vo.getId());
        assertEquals(OrderStatusEnum.PENDING_ACCEPT.getStatus(), vo.getStatus());
        assertEquals("待接单(公海)", vo.getStatusDesc());
    }

    @Test
    void toVOListShouldFillStatusDescForEachOrder() {
        Orders accepted = new Orders();
        accepted.setStatus(OrderStatusEnum.ACCEPTED.getStatus());
        Orders completed = new Orders();
        completed.setStatus(OrderStatusEnum.COMPLETED.getStatus());

        List<OrderVO> vos = orderConverter.toVOList(List.of(accepted, completed));

        assertEquals("已接单(前往中)", vos.get(0).getStatusDesc());
        assertEquals("服务完成", vos.get(1).getStatusDesc());
    }

    @Test
    void toVOShouldKeepUnknownStatusDescEmpty() {
        Orders order = new Orders();
        order.setStatus(99);

        OrderVO vo = orderConverter.toVO(order);

        assertEquals(99, vo.getStatus());
        assertNull(vo.getStatusDesc());
    }
}
