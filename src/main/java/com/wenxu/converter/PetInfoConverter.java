package com.wenxu.converter;

import com.wenxu.dto.PetInfoAddDTO;
import com.wenxu.entity.PetInfo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PetInfoConverter {

    PetInfo toEntity(PetInfoAddDTO dto);
}
