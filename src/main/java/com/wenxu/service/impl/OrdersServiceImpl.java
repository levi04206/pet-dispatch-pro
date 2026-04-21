package com.wenxu.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wenxu.common.OrderStatusEnum;
import com.wenxu.common.SitterAuditStatusEnum;
import com.wenxu.common.SitterWorkStatusEnum;
import com.wenxu.constant.RedisConstants;
import com.wenxu.converter.OrderConverter;
import com.wenxu.dto.OrderCreateDTO;
import com.wenxu.dto.OrderEvaluateDTO;
import com.wenxu.entity.Orders;
import com.wenxu.entity.PetInfo;
import com.wenxu.entity.Sitter;
import com.wenxu.mapper.OrdersMapper;
import com.wenxu.mapper.PetInfoMapper;
import com.wenxu.mapper.SitterMapper;
import com.wenxu.service.OrdersService;
import com.wenxu.service.SitterService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl implements OrdersService {

    @Resource
    private OrdersMapper ordersMapper;

    @Resource
    private SitterMapper sitterMapper;

    @Resource
    private PetInfoMapper petInfoMapper;

    @Resource
    private OrderConverter orderConverter;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SitterService sitterService;

    @Override
    public Orders createOrder(OrderCreateDTO orderCreateDTO, Long userId) {
        // 下单前校验宠物归属，避免用户拿别人的宠物档案创建订单。
        if (!isMyPet(orderCreateDTO.getPetId(), userId)) {
            throw new IllegalArgumentException("宠物不存在或无权下单");
        }

        // 订单创建后先进入待支付状态，支付后才进入公共订单池。
        Orders orders = orderConverter.toEntity(orderCreateDTO);
        orders.setUserId(userId);
        orders.setOrderSn("OD" + IdUtil.getSnowflakeNextIdStr());
        orders.setStatus(OrderStatusEnum.PENDING_PAYMENT.getStatus());
        orders.setTargetSitterId(orderCreateDTO.getTargetSitterId());
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
    public boolean payOrder(String orderSn, Long userId) {
        // 支付时按订单号和当前用户一起查询，避免通过订单号支付他人订单。
        Orders order = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getOrderSn, orderSn)
                .eq(Orders::getUserId, userId));
        if (order == null || !OrderStatusEnum.PENDING_PAYMENT.getStatus().equals(order.getStatus())) {
            return false;
        }

        order.setStatus(OrderStatusEnum.PENDING_ACCEPT.getStatus());
        order.setPayTime(LocalDateTime.now());
        return ordersMapper.updateById(order) > 0;
    }

    @Override
    public List<Orders> getPublicPool(Long userId) {
        // 审核通过的宠托师都可以查看公共订单池；是否能抢单在抢单动作里再校验工作状态。
        if (getApprovedSitter(userId) == null) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getStatus, OrderStatusEnum.PENDING_ACCEPT.getStatus())
                .isNull(Orders::getSitterId)
                .isNull(Orders::getTargetSitterId);
        // 排除自己发布的订单，避免宠托师身份用户自接单。
        queryWrapper.ne(Orders::getUserId, userId);
        queryWrapper.orderByDesc(Orders::getCreateTime);
        return ordersMapper.selectList(queryWrapper);
    }

    @Override
    public List<Orders> listMyOrders(Long userId) {
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, userId);
        queryWrapper.orderByDesc(Orders::getCreateTime);
        return ordersMapper.selectList(queryWrapper);
    }

    @Override
    public List<Orders> listMyServiceOrders(Long userId) {
        Sitter sitter = getApprovedSitter(userId);
        if (sitter == null) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getSitterId, sitter.getId());
        queryWrapper.orderByDesc(Orders::getAcceptTime);
        return ordersMapper.selectList(queryWrapper);
    }

    @Override
    public List<Orders> listMyAssignedOrders(Long userId) {
        // 已审核宠托师可以先查看指定给自己的订单；是否能同意接单在 grabOrder 中再校验工作状态。
        Sitter sitter = getApprovedSitter(userId);
        if (sitter == null) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getTargetSitterId, sitter.getId())
                .eq(Orders::getStatus, OrderStatusEnum.PENDING_ACCEPT.getStatus())
                .isNull(Orders::getSitterId)
                .orderByDesc(Orders::getPayTime);
        return ordersMapper.selectList(queryWrapper);
    }

    @Override
    public Orders getMyOrderDetail(Long orderId, Long userId) {
        return ordersMapper.selectOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getId, orderId)
                .eq(Orders::getUserId, userId));
    }

    @Override
    public Orders getMyServiceOrderDetail(Long orderId, Long userId) {
        Sitter sitter = getApprovedSitter(userId);
        if (sitter == null) {
            return null;
        }

        return ordersMapper.selectOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getId, orderId)
                .eq(Orders::getSitterId, sitter.getId()));
    }

    @Override
    public boolean cancelOrder(Long orderId, Long userId) {
        // 取消订单必须同时满足：当前用户创建、订单处于待支付或待接单。
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, orderId)
                .eq(Orders::getUserId, userId)
                .in(Orders::getStatus,
                        OrderStatusEnum.PENDING_PAYMENT.getStatus(),
                        OrderStatusEnum.PENDING_ACCEPT.getStatus())
                .set(Orders::getStatus, OrderStatusEnum.CANCELED.getStatus())
                .setSql("version = version + 1");
        return ordersMapper.update(null, updateWrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean evaluateOrder(OrderEvaluateDTO orderEvaluateDTO, Long userId) {
        Orders order = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getId, orderEvaluateDTO.getOrderId())
                .eq(Orders::getUserId, userId)
                .eq(Orders::getStatus, OrderStatusEnum.COMPLETED.getStatus()));
        if (order == null || order.getSitterId() == null) {
            return false;
        }

        // 评价只允许订单创建人在服务完成后提交一次，提交后状态进入已评价。
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, orderEvaluateDTO.getOrderId())
                .eq(Orders::getUserId, userId)
                .eq(Orders::getStatus, OrderStatusEnum.COMPLETED.getStatus())
                .set(Orders::getStatus, OrderStatusEnum.EVALUATED.getStatus())
                .set(Orders::getEvaluateRating, orderEvaluateDTO.getRating())
                .set(Orders::getEvaluateContent, orderEvaluateDTO.getContent())
                .set(Orders::getEvaluateTime, LocalDateTime.now())
                .setSql("version = version + 1");
        if (ordersMapper.update(null, updateWrapper) <= 0) {
            return false;
        }

        refreshSitterRating(order.getSitterId(), orderEvaluateDTO.getRating());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean grabOrder(Long orderId, Long userId) {
        Sitter sitter = getGrabCapableSitter(userId);
        if (sitter == null) {
            return false;
        }

        // 抢单使用条件更新：订单仍待接单、未绑定宠托师、不是自己的订单，才能绑定当前宠托师。
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, orderId)
                .ne(Orders::getUserId, userId)
                .eq(Orders::getStatus, OrderStatusEnum.PENDING_ACCEPT.getStatus())
                .isNull(Orders::getSitterId)
                .and(wrapper -> wrapper.isNull(Orders::getTargetSitterId)
                        .or()
                        .eq(Orders::getTargetSitterId, sitter.getId()))
                .set(Orders::getStatus, OrderStatusEnum.ACCEPTED.getStatus())
                .set(Orders::getSitterId, sitter.getId())
                .set(Orders::getAcceptTime, LocalDateTime.now())
                .setSql("version = version + 1");
        if (ordersMapper.update(null, updateWrapper) <= 0) {
            return false;
        }

        // 订单绑定成功后，宠托师进入服务中；失败则抛异常回滚订单更新。
        if (!updateSitterWorkStatus(sitter.getId(), SitterWorkStatusEnum.SERVING.getStatus())) {
            throw new IllegalStateException("宠托师工作状态更新失败");
        }
        return true;
    }

    @Override
    public boolean rejectAssignedOrder(Long orderId, String rejectReason, Long userId) {
        Sitter sitter = getApprovedSitter(userId);
        if (sitter == null) {
            return false;
        }

        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, orderId)
                .eq(Orders::getTargetSitterId, sitter.getId())
                .eq(Orders::getStatus, OrderStatusEnum.PENDING_ACCEPT.getStatus())
                .isNull(Orders::getSitterId)
                .set(Orders::getStatus, OrderStatusEnum.CANCELED.getStatus())
                .set(Orders::getRejectReason, rejectReason)
                .set(Orders::getRejectTime, LocalDateTime.now())
                .setSql("version = version + 1");
        return ordersMapper.update(null, updateWrapper) > 0;
    }

    private Sitter getGrabCapableSitter(Long userId) {
        // 抢单资格 = 审核通过 + 当前处于接单中；服务中或休息中都不能再抢新单。
        Sitter sitter = getApprovedSitter(userId);
        if (sitter == null) {
            return null;
        }
        if (!SitterWorkStatusEnum.ACCEPTING.getStatus().equals(sitter.getWorkStatus())) {
            return null;
        }
        return sitter;
    }

    @Override
    public boolean startService(Long orderId, String picUrl, Long userId) {
        Sitter sitter = getApprovedSitter(userId);
        if (sitter == null) {
            return false;
        }

        // 开始服务必须由承接该订单的宠托师操作，并且订单当前处于已接单状态。
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
    @Transactional(rollbackFor = Exception.class)
    public boolean completeService(Long orderId, String picUrl, Long userId) {
        Sitter sitter = getApprovedSitter(userId);
        if (sitter == null) {
            return false;
        }

        // 完成服务必须由承接该订单的宠托师操作，并且订单当前处于服务中状态。
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, orderId)
                .eq(Orders::getSitterId, sitter.getId())
                .eq(Orders::getStatus, OrderStatusEnum.IN_SERVICE.getStatus())
                .set(Orders::getStatus, OrderStatusEnum.COMPLETED.getStatus())
                .set(Orders::getEndTime, LocalDateTime.now())
                .set(Orders::getEndProof, picUrl)
                .setSql("version = version + 1");
        if (ordersMapper.update(null, updateWrapper) <= 0) {
            return false;
        }

        // 订单完成后，宠托师重新回到接单中，并累计接单数量。
        LambdaUpdateWrapper<Sitter> sitterUpdateWrapper = new LambdaUpdateWrapper<>();
        sitterUpdateWrapper.eq(Sitter::getId, sitter.getId())
                .set(Sitter::getWorkStatus, SitterWorkStatusEnum.ACCEPTING.getStatus())
                .setSql("order_count = order_count + 1");
        if (sitterMapper.update(null, sitterUpdateWrapper) <= 0) {
            throw new IllegalStateException("宠托师履约统计更新失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelTimeoutOrders(LocalDateTime deadline) {
        List<Orders> timeoutOrders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .in(Orders::getStatus,
                        OrderStatusEnum.PENDING_PAYMENT.getStatus(),
                        OrderStatusEnum.PENDING_ACCEPT.getStatus())
                .le(Orders::getCreateTime, deadline));
        if (timeoutOrders.isEmpty()) {
            return 0;
        }

        List<Long> orderIds = timeoutOrders.stream()
                .map(Orders::getId)
                .toList();

        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Orders::getId, orderIds)
                .in(Orders::getStatus,
                        OrderStatusEnum.PENDING_PAYMENT.getStatus(),
                        OrderStatusEnum.PENDING_ACCEPT.getStatus())
                .set(Orders::getStatus, OrderStatusEnum.CANCELED.getStatus())
                .setSql("version = version + 1");
        int canceledCount = ordersMapper.update(null, updateWrapper);

        if (canceledCount > 0) {
            Set<Long> targetSitterIds = timeoutOrders.stream()
                    .map(Orders::getTargetSitterId)
                    .filter(id -> id != null)
                    .collect(Collectors.toSet());
            for (Long sitterId : targetSitterIds) {
                sitterService.restoreWorkStatusToAccepting(sitterId);
            }
        }
        return canceledCount;
    }

    private Sitter getAvailableSitter(Long userId) {
        // 可接单宠托师 = 审核通过 + 工作状态为接单中。
        Sitter sitter = getApprovedSitter(userId);
        if (sitter == null) {
            return null;
        }
        if (!SitterWorkStatusEnum.ACCEPTING.getStatus().equals(sitter.getWorkStatus())) {
            return null;
        }
        return sitter;
    }

    private Sitter getApprovedSitter(Long userId) {
        // 宠托师权限统一从当前用户绑定的 sitter 档案解析，避免 user.id 和 sitter.id 混用。
        Sitter sitter = getSitterByUserId(userId);
        if (sitter == null) {
            return null;
        }
        if (!SitterAuditStatusEnum.APPROVED.getStatus().equals(sitter.getAuditStatus())) {
            return null;
        }
        return sitter;
    }

    private Sitter getSitterByUserId(Long userId) {
        return sitterMapper.selectOne(new LambdaQueryWrapper<Sitter>()
                .eq(Sitter::getUserId, userId));
    }

    private boolean updateSitterWorkStatus(Long sitterId, int workStatus) {
        LambdaUpdateWrapper<Sitter> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Sitter::getId, sitterId)
                .set(Sitter::getWorkStatus, workStatus);
        return sitterMapper.update(null, updateWrapper) > 0;
    }

    private void refreshSitterRating(Long sitterId, Integer rating) {
        Sitter sitter = sitterMapper.selectById(sitterId);
        if (sitter == null) {
            throw new IllegalStateException("宠托师不存在，无法更新评分");
        }

        int completedCount = sitter.getOrderCount() == null ? 0 : sitter.getOrderCount();
        double oldRating = sitter.getRating() == null ? 5.0 : sitter.getRating();
        double newRating = calculateAverageRating(oldRating, completedCount, rating);

        Sitter update = new Sitter();
        update.setId(sitterId);
        update.setRating(newRating);
        if (sitterMapper.updateById(update) <= 0) {
            throw new IllegalStateException("宠托师评分更新失败");
        }
        stringRedisTemplate.opsForZSet().add(RedisConstants.SITTER_RANK_RATING_KEY, sitterId.toString(), newRating);
    }

    private double calculateAverageRating(double oldRating, int completedCount, int newRating) {
        if (completedCount <= 0) {
            return newRating;
        }
        BigDecimal total = BigDecimal.valueOf(oldRating)
                .multiply(BigDecimal.valueOf(Math.max(completedCount - 1, 0)))
                .add(BigDecimal.valueOf(newRating));
        return total.divide(BigDecimal.valueOf(completedCount), 2, RoundingMode.HALF_UP).doubleValue();
    }

    private boolean isMyPet(Long petId, Long userId) {
        Long count = petInfoMapper.selectCount(new LambdaQueryWrapper<PetInfo>()
                .eq(PetInfo::getId, petId)
                .eq(PetInfo::getUserId, userId));
        return count != null && count > 0;
    }
}
