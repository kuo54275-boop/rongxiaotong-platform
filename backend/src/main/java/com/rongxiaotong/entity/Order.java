package com.rongxiaotong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 订单表：买家购买农产品生成的订单
 */
@Data
@TableName("orders")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;       // 订单编号（展示给用户看）
    private Long buyerId;         // 下单买家
    private Long farmerId;        // 供货农户
    private Long addressId;       // 收货地址
    private Double totalAmount;   // 订单总金额（元）
    private String status;        // UNPAID 待付款 / SHIPPED 待发货 / COMPLETED 已完成 / CANCELLED 已取消
    private String createTime;    // 下单时间
}
