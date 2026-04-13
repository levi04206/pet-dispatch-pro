package com.wenxu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wenxu.converter.SitterConverter;
import com.wenxu.dto.SitterApplyDTO;
import com.wenxu.entity.Sitter;
import com.wenxu.mapper.SitterMapper;
import com.wenxu.service.SitterService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class SitterServiceImpl implements SitterService {

    private static final int AUDIT_PENDING = 0;
    private static final int AUDIT_APPROVED = 1;
    private static final int WORK_RESTING = 0;

    @Resource
    private SitterMapper sitterMapper;

    @Resource
    private SitterConverter sitterConverter;

    @Override
    public boolean applySitter(SitterApplyDTO sitterApplyDTO, Long userId) {
        LambdaQueryWrapper<Sitter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Sitter::getUserId, userId);
        if (sitterMapper.selectCount(queryWrapper) > 0) {
            return false;
        }

        Sitter sitter = sitterConverter.toEntity(sitterApplyDTO);
        sitter.setUserId(userId);
        sitter.setAuditStatus(AUDIT_PENDING);
        sitter.setWorkStatus(WORK_RESTING);
        sitter.setOrderCount(0);
        sitter.setRating(5.0);
        sitterMapper.insert(sitter);
        return true;
    }

    @Override
    public boolean auditSitter(Long id, Integer auditStatus) {
        LambdaUpdateWrapper<Sitter> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Sitter::getId, id)
                .set(Sitter::getAuditStatus, auditStatus);
        if (Integer.valueOf(AUDIT_APPROVED).equals(auditStatus)) {
            updateWrapper.set(Sitter::getWorkStatus, WORK_RESTING);
        }
        return sitterMapper.update(null, updateWrapper) > 0;
    }
}
