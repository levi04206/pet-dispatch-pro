package com.wenxu.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wenxu.common.PageResponse;
import com.wenxu.common.Result;
import com.wenxu.entity.OperationLog;
import com.wenxu.service.OperationLogService;
import com.wenxu.vo.OperationLogVO;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/operation-log")
public class AdminOperationLogController {

    private static final String FAILED_MARKER = "[FAILED]";

    @Resource
    private OperationLogService operationLogService;

    /**
     * 管理员分页查询操作审计日志，支持按用户、角色、模块和关键字筛选。
     */
    @GetMapping("/page")
    public Result<PageResponse<OperationLogVO>> pageLogs(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String resultType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            return Result.error("开始时间不能晚于结束时间");
        }
        IPage<OperationLog> page = operationLogService.pageLogs(
                pageNum, pageSize, userId, role, module, keyword, resultType, startTime, endTime);
        return Result.success(PageResponse.of(page, this::toVO));
    }

    /**
     * 管理员查看单条操作日志详情。
     */
    @GetMapping("/{id}")
    public Result<OperationLogVO> getDetail(@PathVariable Long id) {
        OperationLog operationLog = operationLogService.getById(id);
        if (operationLog == null) {
            return Result.error("操作日志不存在");
        }
        return Result.success(toVO(operationLog));
    }

    private OperationLogVO toVO(OperationLog operationLog) {
        OperationLogVO vo = new OperationLogVO();
        vo.setId(operationLog.getId());
        vo.setUserId(operationLog.getUserId());
        vo.setRole(operationLog.getRole());
        vo.setModule(operationLog.getModule());
        vo.setAction(operationLog.getAction());
        vo.setRequestPath(operationLog.getRequestPath());
        vo.setIp(operationLog.getIp());
        vo.setCostTimeMs(operationLog.getCostTimeMs());
        vo.setCreateTime(operationLog.getCreateTime());
        boolean success = operationLog.getAction() == null || !operationLog.getAction().contains(FAILED_MARKER);
        vo.setSuccess(success);
        vo.setResultText(success ? "成功" : "失败");
        return vo;
    }
}
