package com.rongxiaotong.dto;

import com.rongxiaotong.entity.Product;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品展示对象：在商品信息基础上，加上供货农户姓名（继承 + 扩展）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductVO extends Product {

    private String farmerName;    // 供货农户姓名
    private String farmerAddress; // 产地（商品详情页展示）
}
