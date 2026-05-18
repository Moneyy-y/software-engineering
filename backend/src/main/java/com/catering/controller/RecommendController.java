package com.catering.controller;

import com.catering.common.Result;
import com.catering.service.RecommendService;
import com.catering.vo.DishVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    @GetMapping("/list")
    public Result<List<DishVO>> list(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "10") int limit) {
        return Result.ok(recommendService.recommendList(lat, lng, limit));
    }

    @GetMapping("/redblack")
    public Result<Map<String, List<DishVO>>> redblack() {
        return Result.ok(recommendService.redBlackBoard());
    }
}
