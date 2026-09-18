package com.rongxiaotong.dto;

import com.rongxiaotong.entity.LoanApplication;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 贷款申请展示对象：申请信息 + 产品名称 + 联合贷款人姓名
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApplicationVO extends LoanApplication {

    private String productName;    // 贷款产品名称
    private String partnerName;    // 联合贷款人姓名（无则为 null）
}
