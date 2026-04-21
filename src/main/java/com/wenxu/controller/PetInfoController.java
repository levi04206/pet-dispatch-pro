package com.wenxu.controller;

import com.wenxu.annotation.Idempotent;
import com.wenxu.annotation.LogOperation;
import com.wenxu.common.ApiMessages;
import com.wenxu.common.BaseContext;
import com.wenxu.common.Result;
import com.wenxu.converter.PetInfoConverter;
import com.wenxu.dto.PetInfoAddDTO;
import com.wenxu.dto.PetInfoUpdateDTO;
import com.wenxu.entity.PetInfo;
import com.wenxu.service.PetInfoService;
import com.wenxu.vo.PetInfoVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pet")
public class PetInfoController {

    @Resource
    private PetInfoService petInfoService;

    @Resource
    private PetInfoConverter petInfoConverter;

    /**
     * 新增当前用户的宠物档案。
     */
    @PostMapping("/add")
    @Idempotent(expireTime = 5)
    @LogOperation(module = "宠物模块", action = "新增宠物")
    public Result<String> addPet(@Valid @RequestBody PetInfoAddDTO petInfoAddDTO) {
        Long userId = BaseContext.getCurrentId();
        petInfoService.addPet(petInfoAddDTO, userId);
        return Result.success(ApiMessages.PET_ADD_SUCCESS);
    }

    /**
     * 查询当前用户自己的宠物列表。
     */
    @GetMapping("/list")
    public Result<List<PetInfoVO>> listMyPets() {
        Long userId = BaseContext.getCurrentId();
        List<PetInfo> list = petInfoService.listMyPets(userId);
        return Result.success(petInfoConverter.toVOList(list));
    }

    /**
     * 修改当前用户自己的宠物档案，避免修改他人宠物。
     */
    @PutMapping("/{id}")
    @Idempotent(expireTime = 5)
    @LogOperation(module = "宠物模块", action = "修改宠物")
    public Result<String> updatePet(@PathVariable Long id, @Valid @RequestBody PetInfoUpdateDTO petInfoUpdateDTO) {
        Long userId = BaseContext.getCurrentId();
        boolean updated = petInfoService.updateMyPet(id, petInfoUpdateDTO, userId);
        if (!updated) {
            return Result.error(ApiMessages.PET_UPDATE_NOT_FOUND_OR_FORBIDDEN);
        }
        return Result.success(ApiMessages.PET_UPDATE_SUCCESS);
    }

    /**
     * 删除当前用户自己的宠物档案，避免删除他人宠物。
     */
    @DeleteMapping("/{id}")
    @Idempotent(expireTime = 5)
    @LogOperation(module = "宠物模块", action = "删除宠物")
    public Result<String> deletePet(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        boolean deleted = petInfoService.deleteMyPet(id, userId);
        if (!deleted) {
            return Result.error(ApiMessages.PET_NOT_FOUND_OR_FORBIDDEN);
        }
        return Result.success(ApiMessages.PET_DELETE_SUCCESS);
    }
}
