package com.wenxu.controller;

import com.wenxu.common.BaseContext;
import com.wenxu.common.Result;
import com.wenxu.dto.SitterApplyDTO;
import com.wenxu.service.SitterService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sitter")
public class SitterController {

    @Resource
    private SitterService sitterService;

    @PostMapping("/apply")
    public Result<String> applySitter(@Valid @RequestBody SitterApplyDTO sitterApplyDTO) {
        Long currentUserId = BaseContext.getCurrentId();
        boolean applied = sitterService.applySitter(sitterApplyDTO, currentUserId);
        if (!applied) {
            return Result.error("您已提交过申请或已经是宠托师，请勿重复提交");
        }
        return Result.success("入驻申请已提交，请等待管理员审核");
    }
}
