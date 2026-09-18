package com.rongxiaotong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 问答表：农户提问，专家回答
 */
@Data
@TableName("question")
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long farmerId;        // 提问农户
    private String title;         // 问题标题
    private String content;       // 问题详细描述
    private Long expertId;        // 指定回答的专家（可为空，表示所有专家可见）
    private Integer status;       // 0 待回答 / 1 已回答
    private String createTime;    // 提问时间
}
