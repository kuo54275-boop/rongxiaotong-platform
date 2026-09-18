package com.rongxiaotong.dto;

import lombok.Data;

/**
 * 下单请求参数：从购物车结算
 */
@Data
public class OrderCreateDTO {

    private Long addressId;   // 收货地址
}
