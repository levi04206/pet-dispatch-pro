package com.wenxu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wenxu.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}