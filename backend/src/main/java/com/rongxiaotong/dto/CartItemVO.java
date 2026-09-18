package com.rongxiaotong.dto;

import lombok.Data;

/**
 * 购物车条目展示对象：购物车记录 + 商品信息
 */
@Data
public class CartItemVO {

    private Long id;              // 购物车记录 id
    private Long productId;       // 商品 id
    private Integer quantity;     // 数量

    private String productName;   // 商品名称
    private String cover;         // 商品图片
    private Double price;         // 单价
    private String unit;          // 计量单位
    private Integer stock;        // 当前库存
    private String farmerName;    // 供货农户
    private Double subtotal;      // 小计 = 单价 × 数量
}
