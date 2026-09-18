package com.rongxiaotong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 新闻表：首页轮播图 + 资讯展示
 */
@Data
@TableName("news")
public class News {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;         // 标题
    private String cover;         // 封面图
    private String summary;       // 摘要
    private String content;       // 正文内容
    private String createTime;    // 发布时间
}
