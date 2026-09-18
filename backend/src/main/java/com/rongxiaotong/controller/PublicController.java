package com.rongxiaotong.controller;

import com.rongxiaotong.common.Result;
import com.rongxiaotong.dto.HomeVO;
import com.rongxiaotong.entity.News;
import com.rongxiaotong.service.PublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公开接口：不需要登录即可访问（首页展示等）
 */
@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {

    private final PublicService publicService;

    /** 新闻列表 */
    @GetMapping("/news/list")
    public Result<List<News>> newsList() {
        return Result.ok(publicService.newsList());
    }

    /** 首页聚合数据 */
    @GetMapping("/home")
    public Result<HomeVO> home() {
        return Result.ok(publicService.home());
    }
}
