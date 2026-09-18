package com.rongxiaotong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 系统通知表：下单、发货、回答、预约处理等事件的通知
 */
@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;          // 接收通知的用户
    private String type;          // ORDER_NEW / ORDER_SHIPPED / QUESTION_ANSWERED / APPOINTMENT_HANDLED
    private String title;         // 通知标题
    private String content;       // 通知内容
    private Long relatedId;       // 关联业务 id（订单 / 问题 / 预约）
    private Integer isRead;       // 0 未读 / 1 已读
    private String createTime;
}
