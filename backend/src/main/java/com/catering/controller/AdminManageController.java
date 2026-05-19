package com.catering.controller;

import com.catering.annotation.AuditLog;
import com.catering.common.PageResult;
import com.catering.common.Result;
import com.catering.dto.UserQueryDTO;
import com.catering.dto.UserSaveDTO;
import com.catering.entity.SensitiveWord;
import com.catering.entity.Shop;
import com.catering.entity.Stall;
import com.catering.entity.User;
import com.catering.service.DishService;
import com.catering.service.PostService;
import com.catering.service.SensitiveWordService;
import com.catering.service.UserService;
import com.catering.vo.UserVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminManageController {

    private final SensitiveWordService sensitiveWordService;
    private final DishService dishService;
    private final PostService postService;
    private final UserService userService;

    public AdminManageController(SensitiveWordService sensitiveWordService,
                                 DishService dishService, PostService postService,
                                 UserService userService) {
        this.sensitiveWordService = sensitiveWordService;
        this.dishService = dishService;
        this.postService = postService;
        this.userService = userService;
    }

    @GetMapping("/sensitive-word/list")
    public Result<List<SensitiveWord>> listWords() {
        return Result.ok(sensitiveWordService.listAll());
    }

    @PostMapping("/sensitive-word/add")
    @AuditLog("添加敏感词")
    public Result<Void> addWord(@RequestParam String content,
                                @RequestParam(required = false) String category) {
        sensitiveWordService.addWord(content, category);
        return Result.ok();
    }

    @PostMapping("/sensitive-word/delete")
    @AuditLog("删除敏感词")
    public Result<Void> deleteWord(@RequestParam Long wordId) {
        sensitiveWordService.deleteWord(wordId);
        return Result.ok();
    }

    @GetMapping("/shop/list")
    public Result<List<Shop>> listShops() {
        return Result.ok(dishService.listAllShops());
    }

    @PostMapping("/shop/save")
    @AuditLog("保存食堂信息")
    public Result<Shop> saveShop(@RequestBody Shop shop) {
        return Result.ok(dishService.saveShop(shop));
    }

    @GetMapping("/stall/list")
    public Result<List<Stall>> listStalls(@RequestParam Long shopId) {
        return Result.ok(dishService.listStalls(shopId));
    }

    @PostMapping("/stall/save")
    @AuditLog("保存档口信息")
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
    @AuditLog("通过帖子审核")
    public Result<Void> approvePost(@RequestParam Long postId) {
        postService.approvePost(postId);
        return Result.ok();
    }

    @PostMapping("/post/approve/batch")
    @AuditLog("批量通过帖子审核")
    public Result<Void> approvePosts(@RequestBody List<Long> postIds) {
        postService.approvePosts(postIds);
        return Result.ok();
    }

    @PostMapping("/post/reject")
    @AuditLog("拒绝帖子审核")
    public Result<Void> rejectPost(@RequestParam Long postId, 
                                    @RequestParam(required = false) String reason) {
        postService.rejectPost(postId, reason);
        return Result.ok();
    }

    @PostMapping("/post/reject/batch")
    @AuditLog("批量拒绝帖子审核")
    public Result<Void> rejectPosts(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) params.get("postIds");
        List<Long> postIds = ids.stream().map(Long::valueOf).collect(Collectors.toList());
        String reason = (String) params.get("reason");
        postService.rejectPosts(postIds, reason);
        return Result.ok();
    }

    @PostMapping("/post/delete")
    @AuditLog("删除帖子")
    public Result<Void> deletePost(@RequestParam Long postId) {
        postService.deletePost(postId);
        return Result.ok();
    }

    @PostMapping("/post/delete/batch")
    @AuditLog("批量删除帖子")
    public Result<Void> deletePosts(@RequestBody List<Integer> ids) {
        List<Long> postIds = ids.stream().map(Long::valueOf).collect(Collectors.toList());
        postService.deletePosts(postIds);
        return Result.ok();
    }

    @GetMapping("/board/list")
    public Result<Map<String, Object>> getBoardList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(postService.getBoardList(page, size));
    }

    @PostMapping("/board/intervene")
    @AuditLog("红黑榜干预")
    public Result<Void> interveneBoard(@RequestParam Long dishId, @RequestParam String action) {
        postService.interveneBoard(dishId, action);
        return Result.ok();
    }

    @PostMapping("/board/batch/intervene")
    @AuditLog("红黑榜批量干预")
    public Result<Void> batchInterveneBoard(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) params.get("dishIds");
        List<Long> dishIds = ids.stream().map(Long::valueOf).collect(Collectors.toList());
        String action = (String) params.get("action");
        postService.batchInterveneBoard(dishIds, action);
        return Result.ok();
    }

    @GetMapping("/user/list")
    public Result<PageResult<UserVO>> listUsers(UserQueryDTO query,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        PageResult<User> result = userService.listUsers(query, page, size);
        List<UserVO> records = result.getRecords().stream()
                .map(UserVO::of)
                .collect(Collectors.toList());
        return Result.ok(new PageResult<>(records, result.getTotal(), page, size));
    }

    @GetMapping("/user/{userId}")
    public Result<UserVO> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return Result.fail(1004, "用户不存在");
        }
        return Result.ok(UserVO.of(user));
    }

    @PostMapping("/user/save")
    @AuditLog("保存用户")
    public Result<UserVO> saveUser(@RequestBody UserSaveDTO dto) {
        User user = userService.saveUser(dto);
        return Result.ok(UserVO.of(user));
    }

    @PostMapping("/user/delete")
    @AuditLog("删除用户")
    public Result<Void> deleteUser(@RequestParam Long userId) {
        userService.deleteUser(userId);
        return Result.ok();
    }

    @PostMapping("/user/changeRole")
    @AuditLog("修改用户角色")
    public Result<UserVO> changeRole(@RequestParam Long userId, @RequestParam String role) {
        User user = userService.changeRole(userId, role);
        return Result.ok(UserVO.of(user));
    }
}
