package com.wenxu.controller;

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

    @PostMapping("/apply")
    public Result<String> applySitter(@Valid @RequestBody SitterApplyDTO sitterApplyDTO) {
        Long currentUserId = BaseContext.getCurrentId();
        boolean applied = sitterService.applySitter(sitterApplyDTO, currentUserId);
        if (!applied) {
            return Result.error(ApiMessages.SITTER_APPLY_DUPLICATE);
        }
        return Result.success(ApiMessages.SITTER_APPLY_SUCCESS);
    }

    @GetMapping("/me")
    public Result<SitterVO> getMyProfile() {
        Long currentUserId = BaseContext.getCurrentId();
        Sitter sitter = sitterService.getMyProfile(currentUserId);
        if (sitter == null) {
            return Result.error(ApiMessages.SITTER_NOT_FOUND);
        }
        return Result.success(sitterConverter.toVO(sitter));
    }

    @PostMapping("/workStatus")
    public Result<String> switchWorkStatus(@RequestParam Integer workStatus) {
        Long currentUserId = BaseContext.getCurrentId();
        boolean switched = sitterService.switchWorkStatus(currentUserId, workStatus);
        if (!switched) {
            return Result.error(ApiMessages.SITTER_WORK_STATUS_SWITCH_FAILED);
        }
        return Result.success(workStatus == 1 ? ApiMessages.SITTER_WORK_ACCEPTING : ApiMessages.SITTER_WORK_RESTING);
    }
}
