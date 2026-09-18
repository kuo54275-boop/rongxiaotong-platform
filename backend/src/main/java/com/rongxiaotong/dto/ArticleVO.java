package com.rongxiaotong.dto;

import com.rongxiaotong.entity.Article;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识文章展示对象：文章 + 发布专家姓名
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleVO extends Article {

    private String expertName;   // 发布专家姓名
}
