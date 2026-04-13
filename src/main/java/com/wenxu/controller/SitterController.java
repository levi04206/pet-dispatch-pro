package com.wenxu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wenxu.common.Result;
import com.wenxu.common.BaseContext;
import com.wenxu.converter.SitterConverter;
import com.wenxu.dto.SitterApplyDTO;
import com.wenxu.entity.Sitter;
import com.wenxu.mapper.SitterMapper;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sitter")
public class SitterController {

    @Resource
    private SitterMapper sitterInfoMapper;

    @Resource
    private SitterConverter sitterConverter;

    /**
     * 提交宠托师入驻申请
     */
    @PostMapping("/apply")
    public Result<String> applySitter(@Valid @RequestBody SitterApplyDTO sitterApplyDTO) {
        Long currentUserId = BaseContext.getCurrentId();

        // 1. 幂等性校验：检查是不是已经申请过了
        LambdaQueryWrapper<Sitter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Sitter::getUserId, currentUserId);

        if (sitterInfoMapper.selectCount(queryWrapper) > 0) {
            return Result.error("您已提交过申请或已经是宠托师，请勿重复提交！");
        }

        // 2. 补全系统字段
        Sitter sitter = sitterConverter.toEntity(sitterApplyDTO);
        sitter.setUserId(currentUserId);
        //  状态分离，各司其职
        sitter.setAuditStatus(0); // 审核状态：0待审核
        sitter.setWorkStatus(0);  // 工作状态：0休息中 (必须等审核通过了，才能让他改成1)
        sitter.setOrderCount(0); // 新人接单数为 0
        sitter.setRating(5.0);

        // 3. 插入数据库
        sitterInfoMapper.insert(sitter);

        return Result.success("入驻申请已提交，请等待管理员审核！");
    }
}
