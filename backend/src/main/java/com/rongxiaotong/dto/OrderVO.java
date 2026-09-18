package com.rongxiaotong.dto;

import lombok.Data;

import java.util.List;

/**
 * 订单展示对象：订单 + 买卖双方姓名 + 收货地址 + 明细列表
 */
@Data
public class OrderVO {

    private Long id;
    private String orderNo;       // 订单编号
    private Long buyerId;
    private Long farmerId;
    private Double totalAmount;   // 订单总金额
    private String status;        // UNPAID 待付款 / SHIPPED 待发货 / COMPLETED 已完成 / CANCELLED 已取消
    private String createTime;

    private String buyerName;     // 买家姓名
    private String farmerName;    // 供货农户姓名
    private String addressText;   // 收货地址文字
    private List<OrderItemVO> items;   // 订单明细
}
