package com.rongxiaotong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 回答表：专家对问题的回答
 */
@Data
@TableName("answer")
public class Answer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionId;      // 对应问题
    private Long expertId;        // 回答专家
    private String content;       // 回答内容
    private String createTime;    // 回答时间
}
