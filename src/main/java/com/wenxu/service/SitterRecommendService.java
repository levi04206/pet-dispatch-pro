package com.wenxu.service;

import com.wenxu.entity.Sitter;
import com.wenxu.entity.Orders;

import java.util.List;

public interface SitterRecommendService {

    List<Sitter> listTopRatedSitters(Integer limit);

    List<Orders> listSitterReviews(Long sitterId);
}
