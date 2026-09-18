package com.rongxiaotong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 预约表：农户预约专家进行农技指导
 */
@Data
@TableName("appointment")
public class Appointment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long farmerId;        // 预约农户
    private Long expertId;        // 被预约专家
    private String topic;         // 预约主题，如"小麦病虫害防治指导"
    private String description;   // 情况说明
    private String appointTime;   // 期望上门时间
    private String phone;         // 联系电话
    private String status;        // PENDING 待确认 / CONFIRMED 已确认 / DONE 已完成 / CANCELLED 已取消
    private String createTime;    // 提交时间
}
