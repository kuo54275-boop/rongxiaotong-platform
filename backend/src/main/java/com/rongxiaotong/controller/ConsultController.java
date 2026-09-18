package com.rongxiaotong.controller;

import com.rongxiaotong.common.Result;
import com.rongxiaotong.dto.ConsultStatsVO;
import com.rongxiaotong.dto.ContactVO;
import com.rongxiaotong.entity.ConsultMessage;
import com.rongxiaotong.service.ConsultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 在线咨询接口：消息式对话，前端每 3 秒轮询获取新消息
 */
@RestController
@RequestMapping("/consult")
@RequiredArgsConstructor
public class ConsultController {

    private final ConsultService consultService;

    /** 发送消息 */
    @PostMapping("/send")
    public Result<Long> send(@RequestParam Long receiverId, @RequestParam String content) {
        return Result.ok(consultService.send(receiverId, content));
    }

    /** 我与某人的完整对话（轮询用） */
    @GetMapping("/list")
    public Result<List<ConsultMessage>> list(@RequestParam Long with) {
        return Result.ok(consultService.listWith(with));
    }

    /** 我的会话列表（含未读数，用于消息入口展示） */
    @GetMapping("/contacts")
    public Result<List<ContactVO>> contacts() {
        return Result.ok(consultService.contacts());
    }

    /** 通知统计（顶部铃铛红点数据，前端每 10 秒轮询） */
    @GetMapping("/stats")
    public Result<ConsultStatsVO> stats() {
        return Result.ok(consultService.stats());
    }
}
