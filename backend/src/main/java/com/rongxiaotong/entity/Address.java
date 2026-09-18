package com.rongxiaotong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 收货地址表（买家下单时使用）
 */
@Data
@TableName("address")
public class Address {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;          // 所属用户
    private String receiver;      // 收货人姓名
    private String phone;         // 收货人电话
    private String region;        // 所在地区（省市区）
    private String detail;        // 详细地址（街道门牌号）
    private Integer isDefault;    // 1 默认地址 / 0 非默认
}
