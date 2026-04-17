package com.wenxu.service;

import com.wenxu.entity.Sitter;

import java.util.List;

public interface SitterRecommendService {

    List<Sitter> listTopRatedSitters(Integer limit);
}
