package com.catering.controller;

import com.catering.common.PageResult;
import com.catering.common.Result;
import com.catering.dto.FeedbackSubmitDTO;
import com.catering.service.FeedbackService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/submit")
    public Result<Void> submit(@RequestBody FeedbackSubmitDTO dto) {
        feedbackService.submit(dto);
        return Result.ok();
    }

    @GetMapping("/my")
    public Result<List<Map<String, Object>>> my() {
        return Result.ok(feedbackService.myFeedbacks());
    }

    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(feedbackService.list(status, page, size));
    }

    @PostMapping("/accept")
    public Result<Void> accept(@RequestParam Long id) {
        feedbackService.accept(id);
        return Result.ok();
    }

    @PostMapping("/reply")
    public Result<Void> reply(@RequestParam Long id, @RequestParam String content) {
        feedbackService.reply(id, content);
        return Result.ok();
    }

    @PostMapping("/close")
    public Result<Void> close(@RequestParam Long id) {
        feedbackService.close(id);
        return Result.ok();
    }
}
