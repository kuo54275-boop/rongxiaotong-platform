package com.rongxiaotong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 农产品表：农户发布的商品
 */
@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long farmerId;        // 发布农户
    private String name;          // 商品名称，如"红富士苹果"
    private String category;      // 分类：水果 / 蔬菜 / 粮食 / 禽畜 / 水产 / 其他
    private Double price;         // 单价（元）
    private Integer stock;        // 库存（斤/个/件，看 unit）
    private String unit;          // 计量单位，如"斤"、"箱"
    private String cover;         // 商品图片
    private String description;   // 商品介绍
    private Integer status;       // 1 上架 / 0 下架
    private String createTime;    // 发布时间
}
