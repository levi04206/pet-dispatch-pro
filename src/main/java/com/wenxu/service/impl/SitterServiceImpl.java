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

    private static final int USER_STATUS_NORMAL = 1;

    @Resource
    private SitterMapper sitterMapper;

    @Resource
    private SitterConverter sitterConverter;

    @Resource
    private UserMapper userMapper;

    @Override
    public boolean applySitter(SitterApplyDTO sitterApplyDTO, Long userId) {
        User currentUser = userMapper.selectById(userId);
        // 只有正常状态的普通用户可以提交入驻申请；已是宠托师或管理员不能重复进入申请流。
        if (currentUser == null
                || !Integer.valueOf(USER_STATUS_NORMAL).equals(currentUser.getStatus())
                || !UserRoleEnum.USER.name().equals(currentUser.getRole())) {
            return false;
        }
        // 申请手机号必须和当前登录用户一致，避免用 A 用户登录却提交 B 手机号的宠托师档案。
        if (!sitterApplyDTO.getPhone().equals(currentUser.getPhone())) {
            return false;
        }

        LambdaQueryWrapper<Sitter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Sitter::getUserId, userId);
        // 一个用户只允许存在一份宠托师档案，避免重复申请。
        if (sitterMapper.selectCount(queryWrapper) > 0) {
            return false;
        }

        // 新申请默认待审核、休息中，审核通过后才能切换为接单中。
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

        // 按“id + 待审核状态”条件更新，防止并发重复审核同一条申请。
        LambdaUpdateWrapper<Sitter> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Sitter::getId, id)
                .eq(Sitter::getAuditStatus, SitterAuditStatusEnum.PENDING.getStatus())
                .set(Sitter::getAuditStatus, auditStatus);
        if (SitterAuditStatusEnum.APPROVED.getStatus().equals(auditStatus)) {
            updateWrapper.set(Sitter::getWorkStatus, SitterWorkStatusEnum.RESTING.getStatus());
        }
        boolean updated = sitterMapper.update(null, updateWrapper) > 0;
        if (updated && SitterAuditStatusEnum.APPROVED.getStatus().equals(auditStatus)) {
            // 审核通过后同步升级用户角色；失败时抛异常触发事务回滚，避免半完成状态。
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
        // 工作状态只允许在“休息中”和“接单中”之间切换。
        if (!SitterWorkStatusEnum.RESTING.getStatus().equals(workStatus)
                && !SitterWorkStatusEnum.ACCEPTING.getStatus().equals(workStatus)) {
            return false;
        }

        // 只有审核通过的宠托师可以切换工作状态。
        LambdaUpdateWrapper<Sitter> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Sitter::getUserId, userId)
                .eq(Sitter::getAuditStatus, SitterAuditStatusEnum.APPROVED.getStatus())
                .set(Sitter::getWorkStatus, workStatus);
        return sitterMapper.update(null, updateWrapper) > 0;
    }
}
