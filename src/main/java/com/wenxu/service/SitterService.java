package com.wenxu.service;

import com.wenxu.dto.SitterApplyDTO;
import com.wenxu.entity.Sitter;

public interface SitterService {

    boolean applySitter(SitterApplyDTO sitterApplyDTO, Long userId);

    boolean auditSitter(Long id, Integer auditStatus);

    Sitter getMyProfile(Long userId);

    boolean switchWorkStatus(Long userId, Integer workStatus);
}
