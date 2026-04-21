package com.wenxu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wenxu.entity.OperationLog;
import com.wenxu.mapper.OperationLogMapper;
import com.wenxu.service.OperationLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class OperationLogServiceImpl implements OperationLogService {

    private static final String FAILED_MARKER = "[FAILED]";

    @Resource
    private OperationLogMapper operationLogMapper;

    @Override
    @Async("operationLogExecutor")
    public void saveAsync(OperationLog operationLog) {
        try {
            operationLogMapper.insert(operationLog);
        } catch (Exception ex) {
            log.warn("Failed to persist operation log: {}", ex.getMessage());
        }
    }

    @Override
    public IPage<OperationLog> pageLogs(long pageNum, long pageSize, Long userId, String role, String module,
                                        String keyword, String resultType, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<OperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(userId != null, OperationLog::getUserId, userId)
                .eq(role != null && !role.isBlank(), OperationLog::getRole, role)
                .like(module != null && !module.isBlank(), OperationLog::getModule, module)
                .ge(startTime != null, OperationLog::getCreateTime, startTime)
                .le(endTime != null, OperationLog::getCreateTime, endTime)
                .and(keyword != null && !keyword.isBlank(), wrapper -> wrapper
                        .like(OperationLog::getAction, keyword)
                        .or()
                        .like(OperationLog::getRequestPath, keyword)
                        .or()
                        .like(OperationLog::getIp, keyword))
                .orderByDesc(OperationLog::getCreateTime)
                .orderByDesc(OperationLog::getId);
        if ("SUCCESS".equalsIgnoreCase(resultType)) {
            queryWrapper.notLike(OperationLog::getAction, FAILED_MARKER);
        }
        if ("FAILED".equalsIgnoreCase(resultType)) {
            queryWrapper.like(OperationLog::getAction, FAILED_MARKER);
        }
        return operationLogMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
    }

    @Override
    public OperationLog getById(Long id) {
        return operationLogMapper.selectById(id);
    }
}
