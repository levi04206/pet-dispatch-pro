package com.wenxu.controller.admin;

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

    @GetMapping("/pending")
    public Result<List<SitterVO>> listPendingApplications() {
        return Result.success(sitterConverter.toVOList(sitterService.listPendingApplications()));
    }

    @PostMapping("/audit")
    public Result<String> auditSitter(@RequestParam Long id, @RequestParam Integer auditStatus) {
        if (auditStatus != 1 && auditStatus != 2) {
            return Result.error("非法的审核状态码");
        }

        boolean audited = sitterService.auditSitter(id, auditStatus);
        if (!audited) {
            return Result.error("审核失败，找不到该申请记录");
        }

        String msg = (auditStatus == 1) ? "审批通过，该用户正式成为宠托师" : "已驳回该申请";
        return Result.success(msg);
    }
}
