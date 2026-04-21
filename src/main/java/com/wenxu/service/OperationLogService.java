package com.wenxu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wenxu.entity.OperationLog;

public interface OperationLogService {

    void saveAsync(OperationLog operationLog);

    IPage<OperationLog> pageLogs(long pageNum, long pageSize, Long userId, String role, String module, String keyword);
}
