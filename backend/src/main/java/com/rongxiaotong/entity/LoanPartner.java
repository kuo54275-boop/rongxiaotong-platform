package com.rongxiaotong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 联合贷款人表：智能匹配算法为贷款申请推荐的联合贷款农户
 */
@Data
@TableName("loan_partner")
public class LoanPartner {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long applicationId;       // 所属贷款申请
    private Long partnerFarmerId;     // 联合贷款人（农户用户 id）
    private Double score;             // 匹配分数（0-100）
    private Integer status;           // 0 未确认 / 1 已确认
}
