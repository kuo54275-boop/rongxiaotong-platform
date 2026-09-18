package com.rongxiaotong.dto;

import com.rongxiaotong.entity.LoanProduct;
import com.rongxiaotong.entity.News;
import lombok.Data;

import java.util.List;

/**
 * 首页聚合数据：一个接口把首页六个板块的数据全部返回
 */
@Data
public class HomeVO {

    private List<News> news;                  // 轮播新闻
    private List<LoanProduct> loanProducts;   // 金融产品展示
    private List<ExpertVO> experts;           // 专家一览
    private List<ProductVO> products;         // 助农电商（商品）
    private List<ArticleVO> articles;         // 农业知识
    private List<QuestionVO> questions;       // 专家问答展示
}
