package com.rongxiaotong.dto;

import lombok.Data;

/**
 * 贷款申请请求参数
 */
@Data
public class LoanApplyDTO {

    private Long productId;    // 申请的贷款产品
    private Double amount;     // 申请金额（元）
    private Integer term;      // 申请期限（月）
    private String purpose;    // 贷款用途
    private Long partnerId;    // 选中的联合贷款人（可空，表示不联合）
}
