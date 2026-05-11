package com.wenxu.config;

import com.wenxu.common.BaseContext;
import com.wenxu.common.OrderStatusEnum;
import com.wenxu.entity.Orders;
import com.wenxu.entity.Sitter;
import com.wenxu.service.OrdersService;
import com.wenxu.service.SitterRecommendService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

@Configuration
public class PetAgentTools {

    @Bean
    @Description("根据订单ID查询当前登录用户的宠物上门服务订单详情。适用于用户询问订单状态、预约时间、支付金额、宠托师、服务进度等问题。")
    public Function<QueryOrderRequest, OrderDetailResponse> queryOrderDetail(OrdersService ordersService) {
        return request -> {
            Long userId = BaseContext.getCurrentId();
            Orders order = ordersService.getMyOrderDetail(request.orderId(), userId);
            if (order == null) {
                return OrderDetailResponse.notFound(request.orderId());
            }

            return new OrderDetailResponse(
                    true,
                    order.getId(),
                    order.getOrderSn(),
                    order.getUserId(),
                    order.getPetId(),
                    order.getSitterId(),
                    order.getTargetSitterId(),
                    order.getTotalAmount(),
                    order.getPayAmount(),
                    order.getDistance(),
                    order.getStatus(),
                    OrderStatusEnum.getDescByStatus(order.getStatus()),
                    order.getReserveTime(),
                    order.getPayTime(),
                    order.getAcceptTime(),
                    order.getStartTime(),
                    order.getEndTime(),
                    order.getEvaluateRating(),
                    order.getEvaluateContent(),
                    null
            );
        };
    }

    @Bean
    @Description("推荐高评分宠托师。适用于用户询问派单建议、推荐宠托师、找靠谱接单员、谁评分高、谁更适合接单等问题。")
    public Function<RecommendSittersRequest, List<SitterRecommendResponse>> recommendSitters(
            SitterRecommendService sitterRecommendService) {
        return request -> {
            int limit = normalizeLimit(request.limit());
            return sitterRecommendService.listTopRatedSitters(limit)
                    .stream()
                    .map(this::toSitterRecommendResponse)
                    .toList();
        };
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 5;
        }
        return Math.min(limit, 10);
    }

    private SitterRecommendResponse toSitterRecommendResponse(Sitter sitter) {
        return new SitterRecommendResponse(
                sitter.getId(),
                sitter.getUserId(),
                sitter.getRealName(),
                sitter.getPhone(),
                sitter.getWorkStatus(),
                sitter.getAuditStatus(),
                sitter.getOrderCount(),
                sitter.getRating()
        );
    }

    public record QueryOrderRequest(Long orderId) {
    }

    public record RecommendSittersRequest(Integer limit) {
    }

    public record OrderDetailResponse(
            boolean found,
            Long orderId,
            String orderSn,
            Long userId,
            Long petId,
            Long sitterId,
            Long targetSitterId,
            BigDecimal totalAmount,
            BigDecimal payAmount,
            BigDecimal distance,
            Integer status,
            String statusText,
            LocalDateTime reserveTime,
            LocalDateTime payTime,
            LocalDateTime acceptTime,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer evaluateRating,
            String evaluateContent,
            String message
    ) {
        public static OrderDetailResponse notFound(Long orderId) {
            return new OrderDetailResponse(
                    false,
                    orderId,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "未查询到该订单，或者当前用户无权访问该订单"
            );
        }
    }

    public record SitterRecommendResponse(
            Long sitterId,
            Long userId,
            String realName,
            String phone,
            Integer workStatus,
            Integer auditStatus,
            Integer orderCount,
            Double rating
    ) {
    }
}
