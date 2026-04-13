package com.wenxu.service;

import com.wenxu.dto.PetInfoAddDTO;
import com.wenxu.entity.PetInfo;

import java.util.List;

public interface PetInfoService {

    void addPet(PetInfoAddDTO petInfoAddDTO, Long userId);

    List<PetInfo> listMyPets(Long userId);

    boolean deleteMyPet(Long id, Long userId);
}
