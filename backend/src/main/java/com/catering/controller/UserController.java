package com.catering.controller;

import com.catering.common.PageResult;
import com.catering.common.Result;
import com.catering.dto.LoginDTO;
import com.catering.vo.MessageVO;
import com.catering.service.BrowseService;
import com.catering.vo.BrowseHistoryVO;
import com.catering.service.MessageService;
import com.catering.service.UserService;
import com.catering.vo.LoginVO;
import com.catering.vo.ReviewVO;

import java.util.List;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final BrowseService browseService;
    private final MessageService messageService;

    public UserController(UserService userService, BrowseService browseService, MessageService messageService) {
        this.userService = userService;
        this.browseService = browseService;
        this.messageService = messageService;
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        return Result.ok(userService.wechatLogin(dto));
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        return Result.ok(userService.getUserInfo());
    }

    @PostMapping("/favorite/add")
    public Result<Void> addFavorite(@RequestParam Long dishId) {
        userService.addFavorite(dishId);
        return Result.ok();
    }

    @PostMapping("/favorite/remove")
    public Result<Void> removeFavorite(@RequestParam Long dishId) {
        userService.removeFavorite(dishId);
        return Result.ok();
    }

    @GetMapping("/favorite/list")
    public Result<PageResult<Map<String, Object>>> favorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(userService.listFavorites(page, size));
    }

    @GetMapping("/review/my")
    public Result<List<ReviewVO>> myReviews() {
        return Result.ok(userService.listMyReviews());
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            userService.logout(auth.substring(7));
        }
        return Result.ok();
    }

    @PostMapping("/protocol/agree")
    public Result<Void> agreeProtocol() {
        userService.agreeProtocol(userService.getCurrentUserId());
        return Result.ok();
    }

    @PostMapping("/browse")
    public Result<Void> addBrowse(@RequestParam Long dishId) {
        browseService.addBrowse(userService.getCurrentUserId(), dishId, null);
        return Result.ok();
    }

    @GetMapping("/browse/history")
    public Result<List<BrowseHistoryVO>> getBrowseHistory() {
        return Result.ok(browseService.getBrowseHistory(userService.getCurrentUserId()));
    }

    @DeleteMapping("/browse/clear")
    public Result<Void> clearBrowseHistory() {
        browseService.clearBrowseHistory(userService.getCurrentUserId());
        return Result.ok();
    }

    @GetMapping("/message/list")
    public Result<List<MessageVO>> getMessages() {
        return Result.ok(messageService.getMessages(userService.getCurrentUserId()));
    }

    @GetMapping("/message/unread/count")
    public Result<Integer> getUnreadCount() {
        return Result.ok(messageService.getUnreadCount(userService.getCurrentUserId()));
    }

    @PutMapping("/message/read")
    public Result<Void> markAsRead() {
        messageService.markAsRead(userService.getCurrentUserId());
        return Result.ok();
    }

    @DeleteMapping("/message/{id}")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return Result.ok();
    }

    @DeleteMapping("/message/clear")
    public Result<Void> deleteAllMessages() {
        messageService.deleteAll(userService.getCurrentUserId());
        return Result.ok();
    }
}
