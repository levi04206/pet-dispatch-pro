package com.wenxu.controller;

import com.wenxu.annotation.Idempotent;
import com.wenxu.annotation.LogOperation;
import com.wenxu.common.ApiMessages;
import com.wenxu.common.BaseContext;
import com.wenxu.common.Result;
import com.wenxu.converter.SitterConverter;
import com.wenxu.dto.SitterApplyDTO;
import com.wenxu.entity.Sitter;
import com.wenxu.service.SitterService;
import com.wenxu.vo.SitterVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sitter")
public class SitterController {

    @Resource
    private SitterService sitterService;

    @Resource
    private SitterConverter sitterConverter;

    /**
     * 用户提交宠托师入驻申请。
     */
    @PostMapping("/apply")
    @Idempotent(expireTime = 5)
    @LogOperation(module = "宠托师模块", action = "申请入驻")
    public Result<String> applySitter(@Valid @RequestBody SitterApplyDTO sitterApplyDTO) {
        Long currentUserId = BaseContext.getCurrentId();
        boolean applied = sitterService.applySitter(sitterApplyDTO, currentUserId);
        if (!applied) {
            return Result.error(ApiMessages.SITTER_APPLY_DUPLICATE);
        }
        return Result.success(ApiMessages.SITTER_APPLY_SUCCESS);
    }

    /**
     * 查看当前用户绑定的宠托师工作档案。
     */
    @GetMapping("/me")
    public Result<SitterVO> getMyProfile() {
        Long currentUserId = BaseContext.getCurrentId();
        Sitter sitter = sitterService.getMyProfile(currentUserId);
        if (sitter == null) {
            return Result.error(ApiMessages.SITTER_NOT_FOUND);
        }
        return Result.success(sitterConverter.toVO(sitter));
    }

    /**
     * 切换宠托师工作状态：0 休息中，1 接单中。
     */
    @PostMapping("/workStatus")
    @Idempotent(expireTime = 3)
    @LogOperation(module = "宠托师模块", action = "切换工作状态")
    public Result<String> switchWorkStatus(@RequestParam Integer workStatus) {
        Long currentUserId = BaseContext.getCurrentId();
        boolean switched = sitterService.switchWorkStatus(currentUserId, workStatus);
        if (!switched) {
            return Result.error(ApiMessages.SITTER_WORK_STATUS_SWITCH_FAILED);
        }
        return Result.success(workStatus == 1 ? ApiMessages.SITTER_WORK_ACCEPTING : ApiMessages.SITTER_WORK_RESTING);
    }
}
