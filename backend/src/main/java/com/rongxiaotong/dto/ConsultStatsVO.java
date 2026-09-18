package com.rongxiaotong.dto;

import lombok.Data;

/**
 * 通知统计：顶部铃铛红点数据
 */
@Data
public class ConsultStatsVO {

    private long unreadMessages;        // 未读咨询消息数
    private long pendingAppointments;   // 待处理预约数（专家才有）
    private long todoQuestions;         // 待回答问题数（专家才有）
}
