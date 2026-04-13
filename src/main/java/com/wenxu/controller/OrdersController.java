package com.wenxu.controller;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wenxu.common.BaseContext;
import com.wenxu.common.OrderStatusEnum;
import com.wenxu.common.Result;
import com.wenxu.entity.Orders;
import com.wenxu.entity.Sitter;
import com.wenxu.mapper.OrdersMapper;
import com.wenxu.mapper.SitterMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    private static final int SITTER_AUDIT_APPROVED = 1;
    private static final int SITTER_WORK_ACCEPTING = 1;

    @Resource
    private OrdersMapper ordersMapper;

    @Resource
    private SitterMapper sitterMapper;

    /**
     * 第一步：用户发起预约下单
     */
    @PostMapping("/create")
    public Result<Orders> createOrder(@RequestBody Orders orders) {

        // 1. 从 ThreadLocal 口袋里掏出当前下单的大佬是谁
        Long userId = BaseContext.getCurrentId();
        orders.setUserId(userId);

        // 2. 🚨 核心黑科技：生成全局唯一的订单流水号
        // 我们利用 Hutool 提供的“雪花算法 (Snowflake)”，生成一串绝对不会重复的数字
        // 前面拼个 "OD" (Order Dispatch) 显得非常专业
        String orderSn = "OD" + IdUtil.getSnowflakeNextIdStr();
        orders.setOrderSn(orderSn);

        // 3. 初始化订单状态：1 -> 待支付
        orders.setStatus(OrderStatusEnum.PENDING_PAYMENT.getStatus());

        // 4. 模拟计费引擎 (在真实的复杂系统中，这里会根据宠物体重、距离算出价格)
        // 咱们先写死一个极客体验价：99.00 元
        if (orders.getTotalAmount() == null) {
            orders.setTotalAmount(new BigDecimal("99.00"));
            orders.setPayAmount(new BigDecimal("99.00"));
        }

        // 设置一下预估距离
        if (orders.getDistance() == null) {
            orders.setDistance(new BigDecimal("3.5")); // 默认 3.5km
        }

        // 5. 数据落库！
        ordersMapper.insert(orders);

        // 返回刚才创建好的订单信息给前端，前端拿到后就可以弹起微信支付的窗口了
        return Result.success(orders);
    }

    /**
     * 第二步：模拟支付成功回调 (状态机翻转: 1 -> 2)
     */
    @PostMapping("/pay")
    public Result<String> payOrder(@RequestParam String orderSn) {

        // 1. 根据订单流水号查出这笔订单
        // 利用 MyBatis-Plus 的 LambdaQuery 极其优雅
        Orders order = ordersMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Orders>()
                .eq(Orders::getOrderSn, orderSn));

        // 2. 健壮性校验 (大厂基操)
        if (order == null) {
            return Result.error("订单不存在！");
        }

        // 必须是“待支付”状态才能去支付，防止重复扣款或状态错乱
        if (!order.getStatus().equals(OrderStatusEnum.PENDING_PAYMENT.getStatus())) {
            return Result.error("订单状态异常，无法支付！");
        }

        // 3. 核心动作：状态机翻转！
        // 将状态改为 2 (待接单)
        order.setStatus(OrderStatusEnum.PENDING_ACCEPT.getStatus());

        // 记录真实的支付时间
        order.setPayTime(LocalDateTime.now());

        // 4. 更新到数据库
        ordersMapper.updateById(order);

        // 💡 架构师彩蛋：在真实的微服务中，这里通常还会发一条 MQ (消息队列) 消息
        // 广播给全城的宠托师："来活了！快抢！" 咱们后面进阶的时候再加。

        return Result.success("支付成功！订单已抛入公海！");
    }

    /**
     * 第三步：宠托师视角 —— 查看公海大厅 (刷出所有待接单的订单)
     */
    @GetMapping("/publicPool")
    public Result<java.util.List<Orders>> getPublicPool() {
        // 捞出所有状态为 2 (PENDING_ACCEPT) 的订单
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getStatus, OrderStatusEnum.PENDING_ACCEPT.getStatus());
        // 按时间倒序，最新派出的单子在最上面
        queryWrapper.orderByDesc(Orders::getCreateTime);

        java.util.List<Orders> list = ordersMapper.selectList(queryWrapper);
        return Result.success(list);
    }

    /**
     * 第四步：宠托师视角 —— 极限抢单！(状态机翻转: 2 -> 3)
     */
    @PostMapping("/grab")
    public Result<String> grabOrder(@RequestParam Long orderId) {

        // 1. 掏出口袋，看看是哪位宠托师在抢单
        Sitter sitter = getCurrentAvailableSitter();
        if (sitter == null) {
            return Result.error("Please apply as an approved sitter and switch to accepting status first");
        }
        Long sitterId = sitter.getId();

        // 2. 🚨 核心黑科技：乐观锁抢单 (防止两个人同时抢到同一单)
        // 对应的 SQL 逻辑是：
        // UPDATE orders SET status = 3, sitter_id = ?
        // WHERE id = ? AND status = 2

        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();

        updateWrapper.eq(Orders::getId, orderId)
                .eq(Orders::getStatus, OrderStatusEnum.PENDING_ACCEPT.getStatus()) // 必须还是2才能抢！
                .set(Orders::getStatus, OrderStatusEnum.ACCEPTED.getStatus()) // 翻转为3
                // 🚨 注意：确认你的 Orders 实体类里是否有 sitterId (或者 employeeId/workerId) 字段
                // 如果有，把它 set 进去，代表这个单子归属给当前宠托师了
                .set(Orders::getSitterId, sitterId) // 这里暂用 userId 代替接单人，如果你的表里有专用的接单人ID字段(如 sitterId)，请替换！
                .set(Orders::getAcceptTime, LocalDateTime.now());
        // 3. 执行更新，受影响的行数 updateCount
        int updateCount = ordersMapper.update(null, updateWrapper);

        // 4. 判断战果
        if (updateCount > 0) {
            return Result.success("抢单成功！冲鸭！");
        } else {
            return Result.error("手慢了，该订单已被抢走或已取消！");
        }
    }

    /**
     * 第五步升级版：宠托师开始服务 (加入图片凭证)
     */
    @PostMapping("/start")
    public Result<String> startService(@RequestParam Long orderId, @RequestParam String picUrl) {
        Sitter sitter = getCurrentSitter();
        if (sitter == null) {
            return Result.error("Please apply as a sitter first");
        }
        Long sitterId = sitter.getId();

        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, orderId)
                .eq(Orders::getSitterId, sitterId)
                .eq(Orders::getStatus, OrderStatusEnum.ACCEPTED.getStatus())
                .set(Orders::getStatus, OrderStatusEnum.IN_SERVICE.getStatus())
                .set(Orders::getStartTime, LocalDateTime.now())
                .set(Orders::getStartProof, picUrl);

        int count = ordersMapper.update(null, updateWrapper);
        return count > 0 ? Result.success("打卡成功，服务开始！") : Result.error("操作失败，请检查归属权！");
    }

    /**
     * 第六步升级版：宠托师完成服务 (加入图片凭证)
     */
    @PostMapping("/complete")
    public Result<String> completeService(@RequestParam Long orderId, @RequestParam String picUrl) {
        Sitter sitter = getCurrentSitter();
        if (sitter == null) {
            return Result.error("Please apply as a sitter first");
        }
        Long sitterId = sitter.getId();

        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, orderId)
                .eq(Orders::getSitterId, sitterId)
                .eq(Orders::getStatus, OrderStatusEnum.IN_SERVICE.getStatus())
                .set(Orders::getStatus, OrderStatusEnum.COMPLETED.getStatus())
                .set(Orders::getEndTime, LocalDateTime.now())
                .set(Orders::getEndProof, picUrl);

        int count = ordersMapper.update(null, updateWrapper);
        return count > 0 ? Result.success("辛苦了！服务已圆满完成。") : Result.error("操作失败！");
    }

    private Sitter getCurrentSitter() {
        Long userId = BaseContext.getCurrentId();
        return sitterMapper.selectOne(new LambdaQueryWrapper<Sitter>()
                .eq(Sitter::getUserId, userId));
    }

    private Sitter getCurrentAvailableSitter() {
        Sitter sitter = getCurrentSitter();
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


}
