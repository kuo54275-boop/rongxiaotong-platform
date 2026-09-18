package com.rongxiaotong.controller;

import com.rongxiaotong.common.PageResult;
import com.rongxiaotong.common.Result;
import com.rongxiaotong.dto.ArticleVO;
import com.rongxiaotong.dto.DemandVO;
import com.rongxiaotong.dto.ExpertVO;
import com.rongxiaotong.dto.HomeVO;
import com.rongxiaotong.dto.ProductVO;
import com.rongxiaotong.dto.QuestionDetailVO;
import com.rongxiaotong.dto.QuestionVO;
import com.rongxiaotong.entity.LoanProduct;
import com.rongxiaotong.entity.News;
import com.rongxiaotong.entity.User;
import com.rongxiaotong.service.DemandService;
import com.rongxiaotong.service.ExpertService;
import com.rongxiaotong.service.ProductService;
import com.rongxiaotong.service.PublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公开接口：不需要登录即可访问（首页展示、知识库、专家、问答浏览等）
 */
@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {

    private final PublicService publicService;
    private final ExpertService expertService;
    private final ProductService productService;
    private final DemandService demandService;

    /** 新闻列表 */
    @GetMapping("/news/list")
    public Result<List<News>> newsList() {
        return Result.ok(publicService.newsList());
    }

    /** 贷款产品列表 */
    @GetMapping("/loan/products")
    public Result<List<LoanProduct>> loanProducts() {
        return Result.ok(publicService.loanProducts());
    }

    /** 贷款产品详情 */
    @GetMapping("/loan/product/{id}")
    public Result<LoanProduct> loanProductDetail(@PathVariable Long id) {
        return Result.ok(publicService.loanProductDetail(id));
    }

    /** 首页聚合数据 */
    @GetMapping("/home")
    public Result<HomeVO> home() {
        return Result.ok(publicService.home());
    }

    /** 用户公开信息（姓名头像） */
    @GetMapping("/user/{id}/basic")
    public Result<User> userBasic(@PathVariable Long id) {
        return Result.ok(publicService.userBasic(id));
    }

    // ===== 农业知识（浏览） =====

    /** 知识列表（分页 + 分类筛选） */
    @GetMapping("/articles")
    public Result<PageResult<ArticleVO>> articles(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String category) {
        return Result.ok(expertService.pageArticles(page, size, category));
    }

    /** 知识详情（浏览量自动 +1） */
    @GetMapping("/article/{id}")
    public Result<ArticleVO> articleDetail(@PathVariable Long id) {
        return Result.ok(expertService.articleDetail(id));
    }

    // ===== 专家一览（浏览） =====

    /** 专家列表 */
    @GetMapping("/experts")
    public Result<List<ExpertVO>> experts() {
        return Result.ok(expertService.expertList());
    }

    /** 专家详情（按用户 id） */
    @GetMapping("/expert/{userId}")
    public Result<ExpertVO> expertDetail(@PathVariable Long userId) {
        return Result.ok(expertService.expertDetail(userId));
    }

    // ===== 在线问答（浏览） =====

    /** 问答列表（分页，onlyAnswered=1 只看已回答） */
    @GetMapping("/questions")
    public Result<PageResult<QuestionVO>> questions(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer onlyAnswered) {
        return Result.ok(expertService.pageQuestions(page, size, onlyAnswered));
    }

    /** 问答详情（问题 + 全部回答） */
    @GetMapping("/question/{id}")
    public Result<QuestionDetailVO> questionDetail(@PathVariable Long id) {
        return Result.ok(expertService.questionDetail(id));
    }

    // ===== 农产品交易（浏览） =====

    /** 商品列表（分页 + 分类 + 关键词） */
    @GetMapping("/products")
    public Result<PageResult<ProductVO>> products(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "8") long size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return Result.ok(productService.pageProducts(page, size, category, keyword));
    }

    /** 商品详情（含农户姓名和产地） */
    @GetMapping("/product/{id}")
    public Result<ProductVO> productDetail(@PathVariable Long id) {
        return Result.ok(productService.productDetail(id));
    }

    /** 求购需求列表（分页 + 分类） */
    @GetMapping("/demands")
    public Result<PageResult<DemandVO>> demands(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "8") long size,
            @RequestParam(required = false) String category) {
        return Result.ok(demandService.pageDemands(page, size, category));
    }

    /** 求购需求详情 */
    @GetMapping("/demand/{id}")
    public Result<DemandVO> demandDetail(@PathVariable Long id) {
        return Result.ok(demandService.demandDetail(id));
    }
}
