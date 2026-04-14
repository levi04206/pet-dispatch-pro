package com.wenxu.controller;

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
            return Result.error("您已提交过申请或已经是宠托师，请勿重复提交");
        }
        return Result.success("入驻申请已提交，请等待管理员审核");
    }

    @GetMapping("/me")
    public Result<SitterVO> getMyProfile() {
        Long currentUserId = BaseContext.getCurrentId();
        Sitter sitter = sitterService.getMyProfile(currentUserId);
        if (sitter == null) {
            return Result.error("当前用户还不是宠托师");
        }
        return Result.success(sitterConverter.toVO(sitter));
    }

    @PostMapping("/workStatus")
    public Result<String> switchWorkStatus(@RequestParam Integer workStatus) {
        Long currentUserId = BaseContext.getCurrentId();
        boolean switched = sitterService.switchWorkStatus(currentUserId, workStatus);
        if (!switched) {
            return Result.error("状态切换失败，请确认已通过审核且状态值合法");
        }
        return Result.success(workStatus == 1 ? "已切换为接单中" : "已切换为休息中");
    }
}
