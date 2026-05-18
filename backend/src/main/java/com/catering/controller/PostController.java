package com.catering.controller;

import com.catering.common.PageResult;
import com.catering.common.Result;
import com.catering.service.PostService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list(@RequestParam(required = false) String zone) {
        return Result.ok(postService.listApproved(zone));
    }

    @GetMapping("/{postId}")
    public Result<Map<String, Object>> detail(@PathVariable Long postId) {
        return Result.ok(postService.getDetail(postId));
    }

    @PostMapping("/publish")
    public Result<Void> publish(@RequestBody Map<String, String> body) {
        postService.publish(body.get("title"), body.get("content"), body.get("zone"));
        return Result.ok();
    }

    @PostMapping("/comment")
    public Result<Void> comment(@RequestParam Long postId, @RequestParam String content) {
        postService.addComment(postId, content);
        return Result.ok();
    }

    @PostMapping("/like")
    public Result<Void> like(@RequestParam Long postId) {
        postService.toggleLike(postId);
        return Result.ok();
    }

    @GetMapping("/{postId}/comments")
    public Result<List<Map<String, Object>>> comments(@PathVariable Long postId) {
        return Result.ok(postService.listComments(postId));
    }
}
