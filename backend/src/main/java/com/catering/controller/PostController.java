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

    @GetMapping("/my")
    public Result<List<Map<String, Object>>> myPosts() {
        return Result.ok(postService.listMyPosts());
    }

    @GetMapping("/{postId}")
    public Result<Map<String, Object>> detail(@PathVariable Long postId) {
        return Result.ok(postService.getDetail(postId));
    }

    @PostMapping("/publish")
    @SuppressWarnings("unchecked")
    public Result<Void> publish(@RequestBody Map<String, Object> body) {
        List<String> images = null;
        Object rawImages = body.get("images");
        if (rawImages instanceof List) {
            images = (List<String>) rawImages;
        }
        postService.publish((String) body.get("title"), (String) body.get("content"),
                (String) body.get("zone"), images);
        return Result.ok();
    }

    @PostMapping("/resubmit")
    @SuppressWarnings("unchecked")
    public Result<Void> resubmit(@RequestBody Map<String, Object> body) {
        List<String> images = null;
        Object rawImages = body.get("images");
        if (rawImages instanceof List) {
            images = (List<String>) rawImages;
        }
        Object postIdObj = body.get("postId");
        Long postId = postIdObj instanceof Number ? ((Number) postIdObj).longValue() : Long.valueOf(String.valueOf(postIdObj));
        postService.resubmitPost(postId, (String) body.get("title"), (String) body.get("content"),
                (String) body.get("zone"), images);
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
