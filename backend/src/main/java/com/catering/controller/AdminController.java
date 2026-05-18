package com.catering.controller;

import com.catering.common.Result;
import com.catering.dto.AdminLoginDTO;
import com.catering.service.UserService;
import com.catering.vo.LoginVO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody AdminLoginDTO dto) {
        return Result.ok(userService.adminLogin(dto));
    }
}
