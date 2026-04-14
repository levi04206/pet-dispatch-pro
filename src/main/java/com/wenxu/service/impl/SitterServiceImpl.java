package com.wenxu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wenxu.common.UserRoleEnum;
import com.wenxu.converter.SitterConverter;
import com.wenxu.dto.SitterApplyDTO;
import com.wenxu.entity.Sitter;
import com.wenxu.entity.User;
import com.wenxu.mapper.SitterMapper;
import com.wenxu.mapper.UserMapper;
import com.wenxu.service.SitterService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SitterServiceImpl implements SitterService {

    private static final int AUDIT_PENDING = 0;
    private static final int AUDIT_APPROVED = 1;
    private static final int WORK_RESTING = 0;
    private static final int WORK_ACCEPTING = 1;

    @Resource
    private SitterMapper sitterMapper;

    @Resource
    private SitterConverter sitterConverter;

    @Resource
    private UserMapper userMapper;

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
        Sitter sitter = sitterMapper.selectById(id);
        if (sitter == null) {
            return false;
        }
        if (!Integer.valueOf(AUDIT_PENDING).equals(sitter.getAuditStatus())) {
            return false;
        }

        LambdaUpdateWrapper<Sitter> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Sitter::getId, id)
                .set(Sitter::getAuditStatus, auditStatus);
        if (Integer.valueOf(AUDIT_APPROVED).equals(auditStatus)) {
            updateWrapper.set(Sitter::getWorkStatus, WORK_RESTING);
        }
        boolean updated = sitterMapper.update(null, updateWrapper) > 0;
        if (updated && Integer.valueOf(AUDIT_APPROVED).equals(auditStatus)) {
            User user = new User();
            user.setId(sitter.getUserId());
            user.setRole(UserRoleEnum.SITTER.name());
            userMapper.updateById(user);
        }
        return updated;
    }

    @Override
    public List<Sitter> listPendingApplications() {
        LambdaQueryWrapper<Sitter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Sitter::getAuditStatus, AUDIT_PENDING);
        queryWrapper.orderByDesc(Sitter::getCreateTime);
        return sitterMapper.selectList(queryWrapper);
    }

    @Override
    public Sitter getMyProfile(Long userId) {
        return sitterMapper.selectOne(new LambdaQueryWrapper<Sitter>()
                .eq(Sitter::getUserId, userId));
    }

    @Override
    public boolean switchWorkStatus(Long userId, Integer workStatus) {
        if (!Integer.valueOf(WORK_RESTING).equals(workStatus)
                && !Integer.valueOf(WORK_ACCEPTING).equals(workStatus)) {
            return false;
        }

        LambdaUpdateWrapper<Sitter> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Sitter::getUserId, userId)
                .eq(Sitter::getAuditStatus, AUDIT_APPROVED)
                .set(Sitter::getWorkStatus, workStatus);
        return sitterMapper.update(null, updateWrapper) > 0;
    }
}
