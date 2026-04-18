package com.wenxu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wenxu.converter.PetInfoConverter;
import com.wenxu.dto.PetInfoAddDTO;
import com.wenxu.dto.PetInfoUpdateDTO;
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
import static org.mockito.ArgumentMatchers.isNull;
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

    @Test
    void updateMyPetShouldScopeUpdateByPetIdAndUserId() {
        PetInfoUpdateDTO dto = new PetInfoUpdateDTO();
        dto.setPetName("小福");
        dto.setPetType(2);
        dto.setBreed("柯基");
        dto.setWeight(11.5);
        when(petInfoMapper.update(isNull(), any())).thenReturn(1);

        boolean updated = petInfoService.updateMyPet(10L, dto, 100L);

        assertTrue(updated);
        LambdaUpdateWrapper<PetInfo> wrapper = captureUpdateWrapper();
        String sqlSegment = wrapper.getSqlSegment();
        assertTrue(sqlSegment.contains("id"));
        assertTrue(sqlSegment.contains("user_id"));
        String sqlSet = wrapper.getSqlSet();
        assertTrue(sqlSet.contains("name"));
        assertTrue(sqlSet.contains("type"));
        assertTrue(sqlSet.contains("breed"));
        assertTrue(sqlSet.contains("weight"));
        assertTrue(sqlSet.contains("update_time"));
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private LambdaUpdateWrapper<PetInfo> captureUpdateWrapper() {
        ArgumentCaptor<LambdaUpdateWrapper> captor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(petInfoMapper).update(isNull(), captor.capture());
        return captor.getValue();
    }
}
