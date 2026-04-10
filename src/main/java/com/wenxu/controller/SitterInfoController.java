package com.wenxu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wenxu.common.Result;
import com.wenxu.common.BaseContext;
import com.wenxu.entity.Sitter;
import com.wenxu.mapper.SitterInfoMapper;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/sitter")
public class SitterInfoController {

    @Resource
    private SitterInfoMapper sitterInfoMapper; // 记得建一下这个 Mapper 接口哦！

    /**
     * 提交宠托师入驻申请
     */
    @PostMapping("/apply")
    public Result<String> applySitter(@RequestBody Sitter sitter) {
        Long currentUserId = BaseContext.getCurrentId();

        // 1. 幂等性校验：检查是不是已经申请过了
        LambdaQueryWrapper<Sitter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Sitter::getUserId, currentUserId);

        if (sitterInfoMapper.selectCount(queryWrapper) > 0) {
            return Result.error("您已提交过申请或已经是宠托师，请勿重复提交！");
        }

        // 2. 补全系统字段
        sitter.setUserId(currentUserId);
        sitter.setStatus(0); // 0代表待审核

        // 3. 插入数据库
        sitterInfoMapper.insert(sitter);

        return Result.success("入驻申请已提交，请等待管理员审核！");
    }
}