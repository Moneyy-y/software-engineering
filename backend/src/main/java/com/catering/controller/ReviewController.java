package com.catering.controller;

import com.catering.common.PageResult;
import com.catering.common.Result;
import com.catering.dto.ReviewSubmitDTO;
import com.catering.service.ReviewService;
import com.catering.vo.ReviewVO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/submit")
    public Result<Void> submit(@RequestBody ReviewSubmitDTO dto) {
        reviewService.submitReview(dto);
        return Result.ok();
    }

    @GetMapping("/dish/{dishId}")
    public Result<PageResult<ReviewVO>> listByDish(
            @PathVariable Long dishId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(reviewService.listByDish(dishId, page, size));
    }
}
