package com.wenxu.converter;

import com.wenxu.dto.SitterApplyDTO;
import com.wenxu.entity.Sitter;
import com.wenxu.vo.SitterVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SitterConverter {

    Sitter toEntity(SitterApplyDTO dto);

    SitterVO toVO(Sitter sitter);

    List<SitterVO> toVOList(List<Sitter> sitters);
}
