package com.wenxu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wenxu.converter.PetInfoConverter;
import com.wenxu.dto.PetInfoAddDTO;
import com.wenxu.entity.PetInfo;
import com.wenxu.mapper.PetInfoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetInfoServiceImplTest {

    @Mock
    private PetInfoMapper petInfoMapper;

    @Mock
    private PetInfoConverter petInfoConverter;

    @InjectMocks
    private PetInfoServiceImpl petInfoService;

    @Test
    void addPetShouldBindCurrentUserAndCreateTime() {
        PetInfoAddDTO dto = new PetInfoAddDTO();
        PetInfo mappedPet = new PetInfo();

        when(petInfoConverter.toEntity(dto)).thenReturn(mappedPet);
        when(petInfoMapper.insert(mappedPet)).thenReturn(1);

        petInfoService.addPet(dto, 100L);

        assertEquals(100L, mappedPet.getUserId());
        assertNotNull(mappedPet.getCreateTime());
        verify(petInfoMapper).insert(mappedPet);
    }

    @Test
    void listMyPetsShouldScopeQueryByUserId() {
        PetInfo pet = new PetInfo();
        pet.setId(1L);
        when(petInfoMapper.selectList(any())).thenReturn(List.of(pet));

        List<PetInfo> pets = petInfoService.listMyPets(100L);

        assertEquals(List.of(pet), pets);
        LambdaQueryWrapper<PetInfo> wrapper = captureSelectWrapper();
        assertTrue(wrapper.getSqlSegment().contains("user_id"));
    }

    @Test
    void deleteMyPetShouldScopeDeleteByPetIdAndUserId() {
        when(petInfoMapper.delete(any())).thenReturn(1);

        boolean deleted = petInfoService.deleteMyPet(10L, 100L);

        assertTrue(deleted);
        LambdaQueryWrapper<PetInfo> wrapper = captureDeleteWrapper();
        assertTrue(wrapper.getSqlSegment().contains("id"));
        assertTrue(wrapper.getSqlSegment().contains("user_id"));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private LambdaQueryWrapper<PetInfo> captureSelectWrapper() {
        ArgumentCaptor<LambdaQueryWrapper> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(petInfoMapper).selectList(captor.capture());
        return captor.getValue();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private LambdaQueryWrapper<PetInfo> captureDeleteWrapper() {
        ArgumentCaptor<LambdaQueryWrapper> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(petInfoMapper).delete(captor.capture());
        return captor.getValue();
    }
}
