package com.rongxiaotong.dto;

import lombok.Data;

/**
 * 银行侧农户匹配结果：推荐农户 + 各维度得分
 */
@Data
public class FarmerMatchVO {

    private Long recordId;        // 匹配记录 id（标记已联系用）
    private Long userId;          // 农户用户 id
    private String realName;      // 姓名
    private String phone;         // 电话
    private String address;       // 所在地区
    private String crop;          // 主营作物
    private Double landArea;      // 种植面积（亩）
    private Integer creditScore;  // 信用分

    private Double score;         // 综合匹配分（0-100）
    private Double creditPoint;   // 信用分得分（权重 40%）
    private Double abilityPoint;  // 种植规模得分（权重 30%）
    private Double historyPoint;  // 历史履约得分（权重 30%）

    private String reason;        // 一句话推荐理由
}
