package com.wenxu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wenxu.common.BaseContext;
import com.wenxu.common.Result;
import com.wenxu.entity.PetInfo;
import com.wenxu.mapper.PetInfoMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/pet")
public class PetInfoController {

    @Resource
    private PetInfoMapper petInfoMapper;

    /**
     * 添加宠物档案
     */
    @PostMapping("/add")
    public Result<String> addPet(@RequestBody PetInfo petInfo) {
        // 🚨 极客操作：从 ThreadLocal 口袋里直接拿当前登录人的 ID
        Long userId = BaseContext.getCurrentId();
        petInfo.setUserId(userId);
        petInfo.setCreateTime(LocalDateTime.now());

        petInfoMapper.insert(petInfo);
        return Result.success("添加宠物成功");
    }

    /**
     * 查询当前登录用户的所有宠物
     */
    @GetMapping("/list")
    public Result<List<PetInfo>> listMyPets() {
        Long userId = BaseContext.getCurrentId();

        // 构造条件：查询 userId 等于当前登录人 ID 的宠物
        LambdaQueryWrapper<PetInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PetInfo::getUserId, userId);

        List<PetInfo> list = petInfoMapper.selectList(queryWrapper);
        return Result.success(list);
    }

    /**
     * 根据ID删除宠物
     */
    @DeleteMapping("/{id}")
    public Result<String> deletePet(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<PetInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PetInfo::getId, id)
                .eq(PetInfo::getUserId, userId);

        int count = petInfoMapper.delete(queryWrapper);
        if (count == 0) {
            return Result.error("Pet not found or no permission to delete");
        }
        return Result.success("删除成功");
    }
}
