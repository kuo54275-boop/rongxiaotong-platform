package com.rongxiaotong.controller;

import com.rongxiaotong.common.Result;
import com.rongxiaotong.dto.PasswordDTO;
import com.rongxiaotong.entity.User;
import com.rongxiaotong.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 个人信息接口
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 修改基础信息（姓名、电话、头像） */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody User form) {
        userService.updateProfile(form);
        return Result.ok("保存成功", null);
    }

    /** 修改密码 */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody PasswordDTO dto) {
        userService.changePassword(dto);
        return Result.ok("密码修改成功，请重新登录", null);
    }
}
