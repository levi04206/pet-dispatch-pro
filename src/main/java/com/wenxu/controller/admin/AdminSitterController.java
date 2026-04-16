package com.wenxu.controller.admin;

import com.wenxu.common.ApiMessages;
import com.wenxu.common.Result;
import com.wenxu.converter.SitterConverter;
import com.wenxu.service.SitterService;
import com.wenxu.vo.SitterVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sitter")
public class AdminSitterController {

    @Resource
    private SitterService sitterService;

    @Resource
    private SitterConverter sitterConverter;

    /**
     * 管理员查看待审核的宠托师申请列表。
     */
    @GetMapping("/pending")
    public Result<List<SitterVO>> listPendingApplications() {
        return Result.success(sitterConverter.toVOList(sitterService.listPendingApplications()));
    }

    /**
     * 管理员审核宠托师申请：1 通过，2 驳回。
     */
    @PostMapping("/audit")
    public Result<String> auditSitter(@RequestParam Long id, @RequestParam Integer auditStatus) {
        if (auditStatus != 1 && auditStatus != 2) {
            return Result.error(ApiMessages.SITTER_AUDIT_STATUS_INVALID);
        }

        boolean audited = sitterService.auditSitter(id, auditStatus);
        if (!audited) {
            return Result.error(ApiMessages.SITTER_AUDIT_FAILED);
        }

        String msg = (auditStatus == 1) ? ApiMessages.SITTER_AUDIT_APPROVED : ApiMessages.SITTER_AUDIT_REJECTED;
        return Result.success(msg);
    }
}
