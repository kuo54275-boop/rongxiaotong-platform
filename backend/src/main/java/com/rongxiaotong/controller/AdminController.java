package com.rongxiaotong.controller;

import com.rongxiaotong.common.PageResult;
import com.rongxiaotong.common.Result;
import com.rongxiaotong.entity.User;
import com.rongxiaotong.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员接口：用户管理
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    /** 用户列表（分页 + 搜索） */
    @GetMapping("/users")
    public Result<PageResult<User>> users(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(userService.adminUserPage(page, size, keyword));
    }

    /** 启用 / 禁用用户 */
    @PutMapping("/user/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.adminToggleStatus(id, status);
        return Result.ok(status == 1 ? "已启用" : "已禁用", null);
    }

    /** 各角色用户数统计 */
    @GetMapping("/role-counts")
    public Result<List<Long>> roleCounts() {
        return Result.ok(userService.adminRoleCounts());
    }
}
