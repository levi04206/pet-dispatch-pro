package com.wenxu.controller;

import com.wenxu.common.Result;
import com.wenxu.converter.OrderConverter;
import com.wenxu.converter.SitterConverter;
import com.wenxu.service.SitterRecommendService;
import com.wenxu.vo.OrderVO;
import com.wenxu.vo.SitterVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sitter/recommend")
public class SitterRecommendController {

    @Resource
    private SitterRecommendService sitterRecommendService;

    @Resource
    private SitterConverter sitterConverter;

    @Resource
    private OrderConverter orderConverter;

    /**
     * 查询高分宠托师推荐列表，优先读取 Redis 评分榜，榜单为空时回源数据库。
     */
    @GetMapping("/top-rated")
    public Result<List<SitterVO>> listTopRatedSitters(@RequestParam(required = false) Integer limit) {
        return Result.success(sitterConverter.toVOList(sitterRecommendService.listTopRatedSitters(limit)));
    }

    /**
     * 查询某个宠托师的历史评价，供用户指定下单前查看口碑。
     */
    @GetMapping("/{sitterId}/reviews")
    public Result<List<OrderVO>> listSitterReviews(@PathVariable Long sitterId) {
        return Result.success(orderConverter.toVOList(sitterRecommendService.listSitterReviews(sitterId)));
    }
}
