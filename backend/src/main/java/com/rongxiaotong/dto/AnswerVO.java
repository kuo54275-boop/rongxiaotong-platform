package com.rongxiaotong.dto;

import lombok.Data;

/**
 * 回答展示对象：回答内容 + 专家姓名头像
 */
@Data
public class AnswerVO {

    private Long id;
    private Long questionId;
    private Long expertId;
    private String expertName;    // 回答专家姓名
    private String expertAvatar;  // 回答专家头像
    private String content;
    private String createTime;
}
