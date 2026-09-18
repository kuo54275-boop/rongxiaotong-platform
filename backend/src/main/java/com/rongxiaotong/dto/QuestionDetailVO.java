package com.rongxiaotong.dto;

import com.rongxiaotong.entity.Question;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 问答详情：问题 + 提问人 + 全部回答列表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionDetailVO extends Question {

    private String farmerName;          // 提问农户姓名
    private String farmerAvatar;        // 提问农户头像
    private List<AnswerVO> answers;     // 回答列表
}
