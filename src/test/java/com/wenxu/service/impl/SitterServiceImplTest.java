package com.wenxu.service.impl;

import com.wenxu.converter.SitterConverter;
import com.wenxu.dto.SitterApplyDTO;
import com.wenxu.entity.Sitter;
import com.wenxu.mapper.SitterMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SitterServiceImplTest {

    @Mock
    private SitterMapper sitterMapper;

    @Mock
    private SitterConverter sitterConverter;

    @InjectMocks
    private SitterServiceImpl sitterService;

    @Test
    void applySitterShouldInitializePendingProfile() {
        SitterApplyDTO dto = new SitterApplyDTO();
        Sitter mappedSitter = new Sitter();

        when(sitterMapper.selectCount(any())).thenReturn(0L);
        when(sitterConverter.toEntity(dto)).thenReturn(mappedSitter);
        when(sitterMapper.insert(mappedSitter)).thenReturn(1);

        boolean applied = sitterService.applySitter(dto, 100L);

        assertTrue(applied);
        assertEquals(100L, mappedSitter.getUserId());
        assertEquals(0, mappedSitter.getAuditStatus());
        assertEquals(0, mappedSitter.getWorkStatus());
        assertEquals(0, mappedSitter.getOrderCount());
        assertEquals(5.0, mappedSitter.getRating());
        verify(sitterMapper).insert(mappedSitter);
    }

    @Test
    void applySitterShouldRejectDuplicateUserProfile() {
        when(sitterMapper.selectCount(any())).thenReturn(1L);

        boolean applied = sitterService.applySitter(new SitterApplyDTO(), 100L);

        assertFalse(applied);
        verify(sitterConverter, never()).toEntity(any());
        verify(sitterMapper, never()).insert(any());
    }

    @Test
    void auditSitterShouldUpdateAuditStatus() {
        when(sitterMapper.update(any(), any())).thenReturn(1);

        boolean audited = sitterService.auditSitter(10L, 1);

        assertTrue(audited);
        verify(sitterMapper).update(any(), any());
    }
}
