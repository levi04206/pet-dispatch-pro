package com.wenxu.converter;

import com.wenxu.dto.OrderCreateDTO;
import com.wenxu.entity.Orders;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderConverter {

    Orders toEntity(OrderCreateDTO dto);
}
