package com.wenxu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wenxu.common.SitterAuditStatusEnum;
import com.wenxu.common.SitterWorkStatusEnum;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SitterServiceImpl implements SitterService {

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
        sitter.setAuditStatus(SitterAuditStatusEnum.PENDING.getStatus());
        sitter.setWorkStatus(SitterWorkStatusEnum.RESTING.getStatus());
        sitter.setOrderCount(0);
        sitter.setRating(5.0);
        sitterMapper.insert(sitter);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditSitter(Long id, Integer auditStatus) {
        Sitter sitter = sitterMapper.selectById(id);
        if (sitter == null) {
            return false;
        }
        if (!SitterAuditStatusEnum.PENDING.getStatus().equals(sitter.getAuditStatus())) {
            return false;
        }

        LambdaUpdateWrapper<Sitter> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Sitter::getId, id)
                .set(Sitter::getAuditStatus, auditStatus);
        if (SitterAuditStatusEnum.APPROVED.getStatus().equals(auditStatus)) {
            updateWrapper.set(Sitter::getWorkStatus, SitterWorkStatusEnum.RESTING.getStatus());
        }
        boolean updated = sitterMapper.update(null, updateWrapper) > 0;
        if (updated && SitterAuditStatusEnum.APPROVED.getStatus().equals(auditStatus)) {
            User user = new User();
            user.setId(sitter.getUserId());
            user.setRole(UserRoleEnum.SITTER.name());
            if (userMapper.updateById(user) <= 0) {
                throw new IllegalStateException("用户角色升级失败");
            }
        }
        return updated;
    }

    @Override
    public List<Sitter> listPendingApplications() {
        LambdaQueryWrapper<Sitter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Sitter::getAuditStatus, SitterAuditStatusEnum.PENDING.getStatus());
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
        if (!SitterWorkStatusEnum.RESTING.getStatus().equals(workStatus)
                && !SitterWorkStatusEnum.ACCEPTING.getStatus().equals(workStatus)) {
            return false;
        }

        LambdaUpdateWrapper<Sitter> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Sitter::getUserId, userId)
                .eq(Sitter::getAuditStatus, SitterAuditStatusEnum.APPROVED.getStatus())
                .set(Sitter::getWorkStatus, workStatus);
        return sitterMapper.update(null, updateWrapper) > 0;
    }
}
