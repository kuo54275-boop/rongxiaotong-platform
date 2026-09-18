package com.rongxiaotong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 贷款产品表（银行发布）
 */
@Data
@TableName("loan_product")
public class LoanProduct {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long bankId;          // 发布该产品的银行用户 id
    private String name;          // 产品名称，如"惠农贷"
    private Double rate;          // 年利率（%，如 4.35）
    private Double minAmount;     // 最小贷款金额（元）
    private Double maxAmount;     // 最大贷款金额（元）
    private Integer termMonths;   // 贷款期限（月）
    private String description;   // 产品介绍
    private Integer status;       // 1 上架 / 0 下架
}
