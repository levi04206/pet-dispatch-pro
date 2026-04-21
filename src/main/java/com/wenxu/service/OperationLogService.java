package com.wenxu.service;

import com.wenxu.entity.OperationLog;

public interface OperationLogService {

    void saveAsync(OperationLog operationLog);
}
