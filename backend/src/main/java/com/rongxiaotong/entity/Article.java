package com.rongxiaotong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 农业知识表：技术专家维护农业知识库
 */
@Data
@TableName("article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long expertId;        // 发布专家
    private String title;         // 标题
    private String category;      // 分类：种植技术 / 养殖技术 / 病虫害防治 / 政策法规
    private String cover;         // 封面图
    private String content;       // 正文内容
    private Integer views;        // 浏览量
    private String createTime;    // 发布时间
}
