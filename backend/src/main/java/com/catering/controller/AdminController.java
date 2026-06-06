package com.catering.controller;

import com.catering.common.Result;
import com.catering.dto.AdminLoginDTO;
import com.catering.service.UserService;
import com.catering.util.CaptchaStore;
import com.catering.util.CaptchaUtil;
import com.catering.vo.LoginVO;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final CaptchaUtil captchaUtil;
    private final CaptchaStore captchaStore;

    public AdminController(UserService userService, CaptchaUtil captchaUtil, CaptchaStore captchaStore) {
        this.userService = userService;
        this.captchaUtil = captchaUtil;
        this.captchaStore = captchaStore;
    }

    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() throws IOException {
        String captchaCode = captchaUtil.generateCode();
        String captchaKey = UUID.randomUUID().toString();
        String captchaImage = captchaUtil.generateImage(captchaCode);
        captchaStore.save(captchaKey, captchaCode, 5);
        
        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", captchaKey);
        result.put("captchaImage", captchaImage);
        return Result.ok(result);
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody AdminLoginDTO dto) {
        return Result.ok(userService.adminLogin(dto));
    }

    @PostMapping("/refresh")
    public Result<LoginVO> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        return Result.ok(userService.refreshToken(refreshToken));
    }
}
