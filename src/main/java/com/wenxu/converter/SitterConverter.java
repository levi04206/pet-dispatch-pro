package com.wenxu.converter;

import com.wenxu.dto.SitterApplyDTO;
import com.wenxu.entity.Sitter;
import com.wenxu.vo.SitterVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SitterConverter {

    Sitter toEntity(SitterApplyDTO dto);

    SitterVO toVO(Sitter sitter);
}
