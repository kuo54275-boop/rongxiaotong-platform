package com.rongxiaotong.controller;

import com.rongxiaotong.common.Result;
import com.rongxiaotong.entity.Notification;
import com.rongxiaotong.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统通知接口
 */
@RestController
@RequestMapping("/notify")
@RequiredArgsConstructor
public class NotifyController {

    private final NotificationService notificationService;

    /** 我的通知列表（最近 50 条） */
    @GetMapping("/list")
    public Result<List<Notification>> list() {
        return Result.ok(notificationService.myList());
    }

    /** 标记一条已读 */
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return Result.ok(null);
    }

    /** 全部标记已读 */
    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        notificationService.markAllRead();
        return Result.ok("已全部标记为已读", null);
    }
}
