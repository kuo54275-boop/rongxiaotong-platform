package com.rongxiaotong.controller;

import com.rongxiaotong.common.Result;
import com.rongxiaotong.dto.AnswerDTO;
import com.rongxiaotong.dto.QuestionDetailVO;
import com.rongxiaotong.dto.QuestionVO;
import com.rongxiaotong.entity.Article;
import com.rongxiaotong.entity.ExpertProfile;
import com.rongxiaotong.entity.Question;
import com.rongxiaotong.service.ExpertService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 专家助力接口（需要登录）
 */
@RestController
@RequestMapping("/expert")
@RequiredArgsConstructor
public class ExpertController {

    private final ExpertService expertService;

    // ===== 知识库（专家维护） =====

    /** 我发布的知识 */
    @GetMapping("/article/my")
    public Result<List<Article>> myArticles() {
        return Result.ok(expertService.myArticles());
    }

    /** 发布 / 修改知识 */
    @PostMapping("/article/save")
    public Result<Long> saveArticle(@RequestBody Article article) {
        return Result.ok(article.getId() == null ? "发布成功" : "修改成功", expertService.saveArticle(article));
    }

    /** 删除知识 */
    @DeleteMapping("/article/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        expertService.deleteArticle(id);
        return Result.ok("删除成功", null);
    }

    // ===== 专家信息（专家维护） =====

    /** 我的专家信息 */
    @GetMapping("/profile")
    public Result<ExpertProfile> myProfile() {
        return Result.ok(expertService.myProfile());
    }

    /** 修改我的专家信息 */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody ExpertProfile profile) {
        expertService.updateProfile(profile);
        return Result.ok("保存成功", null);
    }

    // ===== 在线问答 =====

    /** 农户：提问 */
    @PostMapping("/question/ask")
    public Result<Long> ask(@RequestBody Question question) {
        return Result.ok("提问成功", expertService.ask(question));
    }

    /** 专家：回答问题 */
    @PostMapping("/question/{id}/answer")
    public Result<Long> answer(@PathVariable Long id, @RequestBody AnswerDTO dto) {
        return Result.ok("回答成功", expertService.answer(id, dto.getContent()));
    }

    /** 专家：待回答的问题 */
    @GetMapping("/question/todo")
    public Result<List<QuestionVO>> todoQuestions() {
        return Result.ok(expertService.todoQuestions());
    }

    /** 农户：我的提问 */
    @GetMapping("/question/my")
    public Result<List<QuestionDetailVO>> myQuestions() {
        return Result.ok(expertService.myQuestions());
    }
}
