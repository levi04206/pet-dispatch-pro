package com.wenxu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wenxu.converter.PetInfoConverter;
import com.wenxu.dto.PetInfoAddDTO;
import com.wenxu.entity.PetInfo;
import com.wenxu.mapper.PetInfoMapper;
import com.wenxu.service.PetInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PetInfoServiceImpl implements PetInfoService {

    @Resource
    private PetInfoMapper petInfoMapper;

    @Resource
    private PetInfoConverter petInfoConverter;

    @Override
    public void addPet(PetInfoAddDTO petInfoAddDTO, Long userId) {
        PetInfo petInfo = petInfoConverter.toEntity(petInfoAddDTO);
        petInfo.setUserId(userId);
        petInfo.setCreateTime(LocalDateTime.now());
        petInfoMapper.insert(petInfo);
    }

    @Override
    public List<PetInfo> listMyPets(Long userId) {
        LambdaQueryWrapper<PetInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PetInfo::getUserId, userId);
        return petInfoMapper.selectList(queryWrapper);
    }

    @Override
    public boolean deleteMyPet(Long id, Long userId) {
        LambdaQueryWrapper<PetInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PetInfo::getId, id)
                .eq(PetInfo::getUserId, userId);
        return petInfoMapper.delete(queryWrapper) > 0;
    }
}
