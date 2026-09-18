package com.rongxiaotong.dto;

import lombok.Data;

/**
 * 联合贷款人匹配结果：推荐一位农户 + 各维度得分（让农户看懂"为什么推荐他"）
 */
@Data
public class PartnerMatchVO {

    private Long userId;            // 被推荐农户的用户 id
    private String realName;        // 姓名
    private String address;         // 所在地区
    private String crop;            // 主营作物
    private Double landArea;        // 种植面积（亩）

    private Double score;           // 综合匹配分（0-100）
    private Double creditPoint;     // 信用分得分（权重 40%）
    private Double abilityPoint;    // 还款能力得分（权重 30%）
    private Double regionPoint;     // 地域相近得分（权重 20%）
    private Double historyPoint;    // 历史合作得分（权重 10%）

    private String reason;          // 一句话推荐理由
}
