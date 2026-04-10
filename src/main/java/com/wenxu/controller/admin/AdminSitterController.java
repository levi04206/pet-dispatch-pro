package com.wenxu.controller.admin;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wenxu.common.Result;
import com.wenxu.entity.Sitter;
import com.wenxu.mapper.SitterMapper;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
@RestController
@RequestMapping("/api/admin/sitter")
public class AdminSitterController {

    @Resource
    private SitterMapper sitterMapper;

    /**
     * W端：管理员审批宠托师入驻申请
     * @param id 宠托师档案的主键ID (sitter表的id)
     * @param auditStatus 审批结果：1通过，2驳回
     */
    @PostMapping("/audit")
    public Result<String> auditSitter(@RequestParam Long id, @RequestParam Integer auditStatus) {
        // 1. 防止瞎传参数
        if (auditStatus != 1 && auditStatus != 2) {
            return Result.error("非法的审核状态码！");
        }

        // 2. 状态机翻转：更新 audit_status
        LambdaUpdateWrapper<Sitter> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Sitter::getId, id)
                .set(Sitter::getAuditStatus, auditStatus);

        // 3. 执行修改并判断受影响行数 (老规矩了)
        int count = sitterMapper.update(null, updateWrapper);

        if (count > 0) {
            String msg = (auditStatus == 1) ? "审批通过！该用户正式成为宠托师！" : "已驳回该申请！";
            return Result.success(msg);
        }

        return Result.error("审批失败，找不到该申请记录！");
    }
}