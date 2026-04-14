package com.wenxu.service;

import com.wenxu.dto.SitterApplyDTO;
import com.wenxu.entity.Sitter;

import java.util.List;

public interface SitterService {

    boolean applySitter(SitterApplyDTO sitterApplyDTO, Long userId);

    boolean auditSitter(Long id, Integer auditStatus);

    List<Sitter> listPendingApplications();

    Sitter getMyProfile(Long userId);

    boolean switchWorkStatus(Long userId, Integer workStatus);
}
