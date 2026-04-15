package com.wenxu.converter;

import com.wenxu.common.SitterAuditStatusEnum;
import com.wenxu.common.SitterWorkStatusEnum;
import com.wenxu.entity.Sitter;
import com.wenxu.vo.SitterVO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SitterConverterTest {

    private final SitterConverter sitterConverter = Mappers.getMapper(SitterConverter.class);

    @Test
    void toVOShouldFillStatusDesc() {
        Sitter sitter = new Sitter();
        sitter.setId(10L);
        sitter.setWorkStatus(SitterWorkStatusEnum.ACCEPTING.getStatus());
        sitter.setAuditStatus(SitterAuditStatusEnum.APPROVED.getStatus());

        SitterVO vo = sitterConverter.toVO(sitter);

        assertEquals(10L, vo.getId());
        assertEquals("接单中", vo.getWorkStatusDesc());
        assertEquals("审核通过", vo.getAuditStatusDesc());
    }

    @Test
    void toVOListShouldFillStatusDescForEachSitter() {
        Sitter pending = new Sitter();
        pending.setWorkStatus(SitterWorkStatusEnum.RESTING.getStatus());
        pending.setAuditStatus(SitterAuditStatusEnum.PENDING.getStatus());
        Sitter serving = new Sitter();
        serving.setWorkStatus(SitterWorkStatusEnum.SERVING.getStatus());
        serving.setAuditStatus(SitterAuditStatusEnum.APPROVED.getStatus());

        List<SitterVO> vos = sitterConverter.toVOList(List.of(pending, serving));

        assertEquals("休息中", vos.get(0).getWorkStatusDesc());
        assertEquals("待审核", vos.get(0).getAuditStatusDesc());
        assertEquals("服务中", vos.get(1).getWorkStatusDesc());
        assertEquals("审核通过", vos.get(1).getAuditStatusDesc());
    }

    @Test
    void toVOShouldKeepUnknownStatusDescEmpty() {
        Sitter sitter = new Sitter();
        sitter.setWorkStatus(99);
        sitter.setAuditStatus(99);

        SitterVO vo = sitterConverter.toVO(sitter);

        assertEquals(99, vo.getWorkStatus());
        assertEquals(99, vo.getAuditStatus());
        assertNull(vo.getWorkStatusDesc());
        assertNull(vo.getAuditStatusDesc());
    }
}
