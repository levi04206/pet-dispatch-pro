package com.wenxu.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wenxu.common.PageResponse;
import com.wenxu.common.Result;
import com.wenxu.entity.OperationLog;
import com.wenxu.service.OperationLogService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/operation-log")
public class AdminOperationLogController {

    @Resource
    private OperationLogService operationLogService;

    /**
     * 管理员分页查询操作审计日志，支持按用户、角色、模块和关键字筛选。
     */
    @GetMapping("/page")
    public Result<PageResponse<OperationLog>> pageLogs(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String keyword) {
        IPage<OperationLog> page = operationLogService.pageLogs(pageNum, pageSize, userId, role, module, keyword);
        return Result.success(PageResponse.of(page));
    }
}
