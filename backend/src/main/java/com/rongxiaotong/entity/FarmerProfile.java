package com.rongxiaotong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 农户扩展信息表：智能匹配算法的核心数据来源
 */
@Data
@TableName("farmer_profile")
public class FarmerProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;          // 对应用户表 id
    private String idCard;        // 身份证号
    private String address;       // 所在地区（如"河南省周口市"）
    private Integer creditScore;  // 信用分（0-100，越高信用越好）
    private String crop;          // 主营作物（如"小麦"）
    private Double landArea;      // 种植面积（亩）
}
