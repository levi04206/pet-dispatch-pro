package com.wenxu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wenxu.common.SitterAuditStatusEnum;
import com.wenxu.constant.RedisConstants;
import com.wenxu.entity.Sitter;
import com.wenxu.mapper.SitterMapper;
import com.wenxu.service.SitterRecommendService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class SitterRecommendServiceImpl implements SitterRecommendService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;

    @Resource
    private SitterMapper sitterMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<Sitter> listTopRatedSitters(Integer limit) {
        int size = normalizeLimit(limit);
        Set<String> sitterIds = stringRedisTemplate.opsForZSet()
                .reverseRange(RedisConstants.SITTER_RANK_RATING_KEY, 0, size - 1L);
        if (sitterIds == null || sitterIds.isEmpty()) {
            return listTopRatedFromDb(size);
        }

        List<Long> ids = parseSitterIds(sitterIds);
        if (ids.isEmpty()) {
            return listTopRatedFromDb(size);
        }

        List<Sitter> sitters = sitterMapper.selectBatchIds(ids);
        if (sitters == null || sitters.isEmpty()) {
            return listTopRatedFromDb(size);
        }

        List<Sitter> ordered = new ArrayList<>(sitters);
        ordered.sort(Comparator.comparingInt(sitter -> ids.indexOf(sitter.getId())));
        return ordered.stream()
                .filter(sitter -> SitterAuditStatusEnum.APPROVED.getStatus().equals(sitter.getAuditStatus()))
                .toList();
    }

    private List<Sitter> listTopRatedFromDb(int limit) {
        LambdaQueryWrapper<Sitter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Sitter::getAuditStatus, SitterAuditStatusEnum.APPROVED.getStatus())
                .orderByDesc(Sitter::getRating)
                .orderByDesc(Sitter::getOrderCount)
                .last("LIMIT " + limit);
        return sitterMapper.selectList(queryWrapper);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private List<Long> parseSitterIds(Set<String> sitterIds) {
        List<Long> ids = new ArrayList<>();
        for (String sitterId : sitterIds) {
            try {
                ids.add(Long.valueOf(sitterId));
            } catch (NumberFormatException ignored) {
                // Redis 榜单可能来自手工调试或旧数据，遇到脏 member 时跳过，避免推荐接口整体失败。
            }
        }
        return ids;
    }
}
