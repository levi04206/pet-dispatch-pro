package com.wenxu.service;

import com.wenxu.dto.SitterApplyDTO;

public interface SitterService {

    boolean applySitter(SitterApplyDTO sitterApplyDTO, Long userId);

    boolean auditSitter(Long id, Integer auditStatus);
}
