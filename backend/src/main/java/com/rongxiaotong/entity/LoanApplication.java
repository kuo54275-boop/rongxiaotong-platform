package com.rongxiaotong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 贷款申请表（融资信息）：农户提交贷款申请后生成
 */
@Data
@TableName("loan_application")
public class LoanApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long farmerId;        // 申请农户
    private Long productId;       // 申请的贷款产品
    private Double amount;        // 申请金额（元）
    private Integer term;         // 申请期限（月）
    private String purpose;       // 贷款用途，如"购买农机"
    private String status;        // PENDING 待审批 / APPROVED 已通过 / REJECTED 已拒绝
    private String applyTime;     // 申请时间
    private String approveTime;   // 审批时间
    private Long bankId;          // 审批银行
}
