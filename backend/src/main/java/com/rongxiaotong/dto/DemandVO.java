package com.rongxiaotong.dto;

import com.rongxiaotong.entity.Demand;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 求购需求展示对象：需求 + 买家姓名电话
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DemandVO extends Demand {

    private String buyerName;     // 买家姓名
    private String buyerPhone;    // 买家电话（农户查看联系时用）
}
