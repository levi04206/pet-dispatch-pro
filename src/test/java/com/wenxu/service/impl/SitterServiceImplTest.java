package com.wenxu.service.impl;

import com.wenxu.common.UserRoleEnum;
import com.wenxu.converter.SitterConverter;
import com.wenxu.dto.SitterApplyDTO;
import com.wenxu.entity.Sitter;
import com.wenxu.entity.User;
import com.wenxu.mapper.SitterMapper;
import com.wenxu.mapper.UserMapper;
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

    @Mock
    private UserMapper userMapper;

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
        Sitter sitter = new Sitter();
        sitter.setId(10L);
        sitter.setUserId(100L);

        when(sitterMapper.selectById(10L)).thenReturn(sitter);
        when(sitterMapper.update(any(), any())).thenReturn(1);

        boolean audited = sitterService.auditSitter(10L, 1);

        assertTrue(audited);
        verify(sitterMapper).update(any(), any());
        verify(userMapper).updateById(any(User.class));
    }

    @Test
    void auditSitterShouldPromoteUserRoleWhenApproved() {
        Sitter sitter = new Sitter();
        sitter.setId(10L);
        sitter.setUserId(100L);

        when(sitterMapper.selectById(10L)).thenReturn(sitter);
        when(sitterMapper.update(any(), any())).thenReturn(1);

        boolean audited = sitterService.auditSitter(10L, 1);

        assertTrue(audited);
        verify(userMapper).updateById(org.mockito.ArgumentMatchers.argThat(user ->
                Long.valueOf(100L).equals(user.getId()) && UserRoleEnum.SITTER.name().equals(user.getRole())));
    }

    @Test
    void auditSitterShouldRejectMissingProfile() {
        when(sitterMapper.selectById(10L)).thenReturn(null);

        boolean audited = sitterService.auditSitter(10L, 1);

        assertFalse(audited);
        verify(sitterMapper, never()).update(any(), any());
        verify(userMapper, never()).updateById(any());
    }
}
