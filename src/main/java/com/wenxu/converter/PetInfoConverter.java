package com.wenxu.converter;

import com.wenxu.dto.PetInfoAddDTO;
import com.wenxu.entity.PetInfo;
import com.wenxu.vo.PetInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PetInfoConverter {

    PetInfo toEntity(PetInfoAddDTO dto);

    PetInfoVO toVO(PetInfo petInfo);

    List<PetInfoVO> toVOList(List<PetInfo> petInfos);
}
