package com.rongxiaotong.dto;

import com.rongxiaotong.entity.Question;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 问答展示对象：问题 + 提问农户姓名 + 专家回答内容
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionVO extends Question {

    private String farmerName;        // 提问农户姓名
    private String answerContent;     // 回答内容（未回答则为 null）
    private String answerExpertName;  // 回答专家姓名
}
