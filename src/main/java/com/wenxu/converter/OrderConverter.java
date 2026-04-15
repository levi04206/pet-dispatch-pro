package com.wenxu.converter;

import com.wenxu.common.OrderStatusEnum;
import com.wenxu.dto.OrderCreateDTO;
import com.wenxu.entity.Orders;
import com.wenxu.vo.OrderVO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderConverter {

    Orders toEntity(OrderCreateDTO dto);

    OrderVO toVO(Orders orders);

    List<OrderVO> toVOList(List<Orders> orders);

    @AfterMapping
    default void fillStatusDesc(Orders orders, @MappingTarget OrderVO orderVO) {
        if (orders == null || orderVO == null) {
            return;
        }
        orderVO.setStatusDesc(OrderStatusEnum.getDescByStatus(orders.getStatus()));
    }
}
