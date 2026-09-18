package com.rongxiaotong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 银行侧智能匹配记录表：算法为银行推荐符合贷款产品条件的农户
 */
@Data
@TableName("match_record")
public class MatchRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long bankId;          // 发起匹配的银行
    private Long farmerId;        // 被推荐的农户
    private Long productId;       // 匹配的贷款产品
    private Double score;         // 匹配分数（0-100）
    private Integer status;       // 0 已推荐 / 1 已联系
    private String createTime;    // 匹配时间
}
