package com.wenxu.converter;

import com.wenxu.dto.SitterApplyDTO;
import com.wenxu.entity.Sitter;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SitterConverter {

    Sitter toEntity(SitterApplyDTO dto);
}
