package com.wenxu.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wenxu.common.OrderStatusEnum;
import com.wenxu.converter.OrderConverter;
import com.wenxu.dto.OrderCreateDTO;
import com.wenxu.entity.Orders;
import com.wenxu.entity.PetInfo;
import com.wenxu.entity.Sitter;
import com.wenxu.mapper.OrdersMapper;
import com.wenxu.mapper.PetInfoMapper;
import com.wenxu.mapper.SitterMapper;
import com.wenxu.service.OrdersService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrdersServiceImpl implements OrdersService {

    private static final int SITTER_AUDIT_APPROVED = 1;
    private static final int SITTER_WORK_ACCEPTING = 1;

    @Resource
    private OrdersMapper ordersMapper;

    @Resource
    private SitterMapper sitterMapper;

    @Resource
    private PetInfoMapper petInfoMapper;

    @Resource
    private OrderConverter orderConverter;

    @Override
    public Orders createOrder(OrderCreateDTO orderCreateDTO, Long userId) {
        if (!isMyPet(orderCreateDTO.getPetId(), userId)) {
            throw new IllegalArgumentException("宠物不存在或无权下单");
        }

        Orders orders = orderConverter.toEntity(orderCreateDTO);
        orders.setUserId(userId);
        orders.setOrderSn("OD" + IdUtil.getSnowflakeNextIdStr());
        orders.setStatus(OrderStatusEnum.PENDING_PAYMENT.getStatus());
        orders.setVersion(0);

        if (orders.getTotalAmount() == null) {
            orders.setTotalAmount(new BigDecimal("99.00"));
            orders.setPayAmount(new BigDecimal("99.00"));
        } else {
            orders.setPayAmount(orders.getTotalAmount());
        }

        if (orders.getDistance() == null) {
            orders.setDistance(new BigDecimal("3.5"));
        }

        ordersMapper.insert(orders);
        return orders;
    }

    @Override
    public boolean payOrder(String orderSn) {
        Orders order = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getOrderSn, orderSn));
        if (order == null || !OrderStatusEnum.PENDING_PAYMENT.getStatus().equals(order.getStatus())) {
            return false;
        }

        order.setStatus(OrderStatusEnum.PENDING_ACCEPT.getStatus());
        order.setPayTime(LocalDateTime.now());
        return ordersMapper.updateById(order) > 0;
    }

    @Override
    public List<Orders> getPublicPool() {
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getStatus, OrderStatusEnum.PENDING_ACCEPT.getStatus());
        queryWrapper.orderByDesc(Orders::getCreateTime);
        return ordersMapper.selectList(queryWrapper);
    }

    @Override
    public boolean grabOrder(Long orderId, Long userId) {
        Sitter sitter = getAvailableSitter(userId);
        if (sitter == null) {
            return false;
        }

        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, orderId)
                .eq(Orders::getStatus, OrderStatusEnum.PENDING_ACCEPT.getStatus())
                .set(Orders::getStatus, OrderStatusEnum.ACCEPTED.getStatus())
                .set(Orders::getSitterId, sitter.getId())
                .set(Orders::getAcceptTime, LocalDateTime.now())
                .setSql("version = version + 1");
        return ordersMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean startService(Long orderId, String picUrl, Long userId) {
        Sitter sitter = getSitterByUserId(userId);
        if (sitter == null) {
            return false;
        }

        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, orderId)
                .eq(Orders::getSitterId, sitter.getId())
                .eq(Orders::getStatus, OrderStatusEnum.ACCEPTED.getStatus())
                .set(Orders::getStatus, OrderStatusEnum.IN_SERVICE.getStatus())
                .set(Orders::getStartTime, LocalDateTime.now())
                .set(Orders::getStartProof, picUrl)
                .setSql("version = version + 1");
        return ordersMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean completeService(Long orderId, String picUrl, Long userId) {
        Sitter sitter = getSitterByUserId(userId);
        if (sitter == null) {
            return false;
        }

        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, orderId)
                .eq(Orders::getSitterId, sitter.getId())
                .eq(Orders::getStatus, OrderStatusEnum.IN_SERVICE.getStatus())
                .set(Orders::getStatus, OrderStatusEnum.COMPLETED.getStatus())
                .set(Orders::getEndTime, LocalDateTime.now())
                .set(Orders::getEndProof, picUrl)
                .setSql("version = version + 1");
        return ordersMapper.update(null, updateWrapper) > 0;
    }

    private Sitter getAvailableSitter(Long userId) {
        Sitter sitter = getSitterByUserId(userId);
        if (sitter == null) {
            return null;
        }
        if (!Integer.valueOf(SITTER_AUDIT_APPROVED).equals(sitter.getAuditStatus())) {
            return null;
        }
        if (!Integer.valueOf(SITTER_WORK_ACCEPTING).equals(sitter.getWorkStatus())) {
            return null;
        }
        return sitter;
    }

    private Sitter getSitterByUserId(Long userId) {
        return sitterMapper.selectOne(new LambdaQueryWrapper<Sitter>()
                .eq(Sitter::getUserId, userId));
    }

    private boolean isMyPet(Long petId, Long userId) {
        Long count = petInfoMapper.selectCount(new LambdaQueryWrapper<PetInfo>()
                .eq(PetInfo::getId, petId)
                .eq(PetInfo::getUserId, userId));
        return count != null && count > 0;
    }
}
