package com.wenxu.service.impl;

import com.wenxu.entity.OperationLog;
import com.wenxu.mapper.OperationLogMapper;
import com.wenxu.service.OperationLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OperationLogServiceImpl implements OperationLogService {

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
}
