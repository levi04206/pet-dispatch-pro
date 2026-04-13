package com.wenxu.controller;

import com.wenxu.common.BaseContext;
import com.wenxu.common.Result;
import com.wenxu.dto.PetInfoAddDTO;
import com.wenxu.entity.PetInfo;
import com.wenxu.service.PetInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pet")
public class PetInfoController {

    @Resource
    private PetInfoService petInfoService;

    @PostMapping("/add")
    public Result<String> addPet(@Valid @RequestBody PetInfoAddDTO petInfoAddDTO) {
        Long userId = BaseContext.getCurrentId();
        petInfoService.addPet(petInfoAddDTO, userId);
        return Result.success("添加宠物成功");
    }

    @GetMapping("/list")
    public Result<List<PetInfo>> listMyPets() {
        Long userId = BaseContext.getCurrentId();
        List<PetInfo> list = petInfoService.listMyPets(userId);
        return Result.success(list);
    }

    @DeleteMapping("/{id}")
    public Result<String> deletePet(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        boolean deleted = petInfoService.deleteMyPet(id, userId);
        if (!deleted) {
            return Result.error("宠物不存在或无权删除");
        }
        return Result.success("删除成功");
    }
}
