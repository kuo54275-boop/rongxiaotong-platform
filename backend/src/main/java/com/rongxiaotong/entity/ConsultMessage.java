package com.rongxiaotong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 在线咨询消息表：农户与专家的对话消息（简单轮询式聊天）
 */
@Data
@TableName("consult_message")
public class ConsultMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long senderId;        // 发送者
    private Long receiverId;      // 接收者
    private String content;       // 消息内容
    private Integer isRead;       // 0 未读 / 1 已读
    private String createTime;    // 发送时间
}
