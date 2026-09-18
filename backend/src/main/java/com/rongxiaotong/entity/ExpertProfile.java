package com.rongxiaotong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 技术专家信息表
 */
@Data
@TableName("expert_profile")
public class ExpertProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;          // 对应用户表 id
    private String title;         // 职称，如"高级农艺师"
    private String specialty;     // 擅长领域，如"水稻种植、病虫害防治"
    private String intro;         // 专家介绍
    private Integer years;        // 从业年限
    private Double rating;        // 评分（5 分制）
    private Integer serviceCount; // 累计服务次数
    private Integer status;       // 1 正常 / 0 停用
}
