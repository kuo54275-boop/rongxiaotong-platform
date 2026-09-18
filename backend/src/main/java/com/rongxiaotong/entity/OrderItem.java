package com.rongxiaotong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 订单明细表：一个订单可能包含多个商品，每个商品一条明细
 */
@Data
@TableName("order_item")
public class OrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;         // 所属订单
    private Long productId;       // 商品
    private Double price;         // 成交单价（下单时的价格）
    private Integer quantity;     // 购买数量
}
