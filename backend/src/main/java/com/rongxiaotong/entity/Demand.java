package com.rongxiaotong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 求购需求表：买家发布采购需求，农户可查看并联系
 */
@Data
@TableName("demand")
public class Demand {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long buyerId;         // 发布需求的买家
    private String title;         // 需求标题，如"求购优质红富士苹果"
    private String category;      // 品类：水果 / 蔬菜 / 粮食 / 禽畜 / 水产 / 其他
    private Double quantity;      // 需求数量
    private String unit;          // 计量单位
    private Double budget;        // 预算金额（元）
    private String deadline;      // 期望供货时间
    private String description;   // 需求详情
    private Integer status;       // 1 有效 / 0 已结束
    private String createTime;    // 发布时间
}
