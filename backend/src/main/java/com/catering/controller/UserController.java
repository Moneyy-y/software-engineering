package com.catering.controller;

import com.catering.common.PageResult;
import com.catering.common.Result;
import com.catering.dto.LoginDTO;
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

    public UserController(UserService userService) {
        this.userService = userService;
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
}
