package com.wenxu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wenxu.entity.OperationLog;

import java.time.LocalDateTime;

public interface OperationLogService {

    void saveAsync(OperationLog operationLog);

    IPage<OperationLog> pageLogs(long pageNum, long pageSize, Long userId, String role, String module,
                                 String keyword, String resultType, LocalDateTime startTime, LocalDateTime endTime);

    OperationLog getById(Long id);
}
