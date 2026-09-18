package com.rongxiaotong.dto;

import lombok.Data;

/**
 * 订单明细展示对象：明细 + 商品名称图片
 */
@Data
public class OrderItemVO {

    private Long id;
    private Long productId;
    private String productName;   // 商品名称
    private String cover;         // 商品图片
    private String unit;          // 计量单位
    private Double price;         // 成交单价
    private Integer quantity;     // 数量
    private Double subtotal;      // 小计
}
