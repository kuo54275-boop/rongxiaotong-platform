package com.rongxiaotong.controller;

import com.rongxiaotong.common.Result;
import com.rongxiaotong.dto.LoginDTO;
import com.rongxiaotong.dto.LoginVO;
import com.rongxiaotong.dto.RegisterDTO;
import com.rongxiaotong.entity.User;
import com.rongxiaotong.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 账号接口：注册 / 登录 / 当前用户信息
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 注册 */
    @PostMapping("/register")
    public Result<LoginVO> register(@RequestBody RegisterDTO dto) {
        return Result.ok("注册成功", authService.register(dto));
    }

    /** 登录 */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        return Result.ok("登录成功", authService.login(dto));
    }

    /** 当前登录用户信息（需要携带 token） */
    @GetMapping("/info")
    public Result<User> info() {
        return Result.ok(authService.info());
    }
}
