package com.catering.controller;

import com.catering.common.PageResult;
import com.catering.common.Result;
import com.catering.entity.SensitiveWord;
import com.catering.entity.Shop;
import com.catering.entity.Stall;
import com.catering.service.DishService;
import com.catering.service.PostService;
import com.catering.service.SensitiveWordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminManageController {

    private final SensitiveWordService sensitiveWordService;
    private final DishService dishService;
    private final PostService postService;

    public AdminManageController(SensitiveWordService sensitiveWordService,
                                 DishService dishService, PostService postService) {
        this.sensitiveWordService = sensitiveWordService;
        this.dishService = dishService;
        this.postService = postService;
    }

    @GetMapping("/sensitive-word/list")
    public Result<List<SensitiveWord>> listWords() {
        return Result.ok(sensitiveWordService.listAll());
    }

    @PostMapping("/sensitive-word/add")
    public Result<Void> addWord(@RequestParam String content,
                                @RequestParam(required = false) String category) {
        sensitiveWordService.addWord(content, category);
        return Result.ok();
    }

    @PostMapping("/sensitive-word/delete")
    public Result<Void> deleteWord(@RequestParam Long wordId) {
        sensitiveWordService.deleteWord(wordId);
        return Result.ok();
    }

    @GetMapping("/shop/list")
    public Result<List<Shop>> listShops() {
        return Result.ok(dishService.listAllShops());
    }

    @PostMapping("/shop/save")
    public Result<Shop> saveShop(@RequestBody Shop shop) {
        return Result.ok(dishService.saveShop(shop));
    }

    @GetMapping("/stall/list")
    public Result<List<Stall>> listStalls(@RequestParam Long shopId) {
        return Result.ok(dishService.listStalls(shopId));
    }

    @PostMapping("/stall/save")
    public Result<Stall> saveStall(@RequestBody Stall stall) {
        return Result.ok(dishService.saveStall(stall));
    }

    @GetMapping("/post/pending")
    public Result<PageResult<Map<String, Object>>> pendingPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(postService.listPending(page, size));
    }

    @PostMapping("/post/approve")
    public Result<Void> approvePost(@RequestParam Long postId) {
        postService.approvePost(postId);
        return Result.ok();
    }

    @PostMapping("/post/reject")
    public Result<Void> rejectPost(@RequestParam Long postId) {
        postService.rejectPost(postId);
        return Result.ok();
    }
}
