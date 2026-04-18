package com.wenxu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wenxu.common.SitterAuditStatusEnum;
import com.wenxu.entity.Orders;
import com.wenxu.entity.Sitter;
import com.wenxu.mapper.OrdersMapper;
import com.wenxu.mapper.SitterMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SitterRecommendServiceImplTest {

    @Mock
    private SitterMapper sitterMapper;

    @Mock
    private OrdersMapper ordersMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    private SitterRecommendServiceImpl sitterRecommendService;

    @Test
    void listTopRatedSittersShouldReadRedisRankFirst() {
        Sitter first = approvedSitter(20L);
        Sitter second = approvedSitter(10L);
        Set<String> ids = new LinkedHashSet<>(List.of("20", "10"));
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.reverseRange("pet:sitter:rank:rating", 0, 2L)).thenReturn(ids);
        when(sitterMapper.selectBatchIds(List.of(20L, 10L))).thenReturn(List.of(second, first));

        List<Sitter> result = sitterRecommendService.listTopRatedSitters(3);

        assertEquals(List.of(first, second), result);
    }

    @Test
    void listTopRatedSittersShouldFallbackToDbWhenRedisEmpty() {
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.reverseRange("pet:sitter:rank:rating", 0, 9L)).thenReturn(Set.of());
        when(sitterMapper.selectList(any())).thenReturn(List.of(approvedSitter(10L)));

        List<Sitter> result = sitterRecommendService.listTopRatedSitters(null);

        assertEquals(1, result.size());
        LambdaQueryWrapper<Sitter> wrapper = captureSelectWrapper();
        assertTrue(wrapper.getSqlSegment().contains("audit_status"));
        assertTrue(wrapper.getSqlSegment().contains("ORDER BY"));
    }

    @Test
    void listTopRatedSittersShouldIgnoreDirtyRedisMembersAndFallbackToDb() {
        Set<String> ids = new LinkedHashSet<>(List.of("bad-id"));
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.reverseRange("pet:sitter:rank:rating", 0, 4L)).thenReturn(ids);
        when(sitterMapper.selectList(any())).thenReturn(List.of(approvedSitter(30L)));

        List<Sitter> result = sitterRecommendService.listTopRatedSitters(5);

        assertEquals(List.of(approvedSitter(30L)), result);
    }

    @Test
    void listSitterReviewsShouldQueryEvaluatedOrders() {
        Orders order = new Orders();
        order.setId(40L);
        when(ordersMapper.selectList(any())).thenReturn(List.of(order));

        List<Orders> result = sitterRecommendService.listSitterReviews(10L);

        assertEquals(List.of(order), result);
        verify(ordersMapper).selectList(any());
    }

    private Sitter approvedSitter(Long id) {
        Sitter sitter = new Sitter();
        sitter.setId(id);
        sitter.setAuditStatus(SitterAuditStatusEnum.APPROVED.getStatus());
        return sitter;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private LambdaQueryWrapper<Sitter> captureSelectWrapper() {
        ArgumentCaptor<LambdaQueryWrapper> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(sitterMapper).selectList(captor.capture());
        return captor.getValue();
    }
}
