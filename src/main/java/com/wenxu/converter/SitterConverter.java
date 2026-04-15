package com.wenxu.converter;

import com.wenxu.common.SitterAuditStatusEnum;
import com.wenxu.common.SitterWorkStatusEnum;
import com.wenxu.dto.SitterApplyDTO;
import com.wenxu.entity.Sitter;
import com.wenxu.vo.SitterVO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SitterConverter {

    Sitter toEntity(SitterApplyDTO dto);

    SitterVO toVO(Sitter sitter);

    List<SitterVO> toVOList(List<Sitter> sitters);

    @AfterMapping
    default void fillStatusDesc(Sitter sitter, @MappingTarget SitterVO sitterVO) {
        if (sitter == null || sitterVO == null) {
            return;
        }
        sitterVO.setWorkStatusDesc(SitterWorkStatusEnum.getDescByStatus(sitter.getWorkStatus()));
        sitterVO.setAuditStatusDesc(SitterAuditStatusEnum.getDescByStatus(sitter.getAuditStatus()));
    }
}
