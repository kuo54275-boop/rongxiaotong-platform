package com.rongxiaotong.dto;

import lombok.Data;

/**
 * 咨询会话列表项：一个联系人 + 最后一条消息 + 未读数
 */
@Data
public class ContactVO {

    private Long userId;          // 对方用户 id
    private String realName;      // 对方姓名
    private String avatar;        // 对方头像
    private String lastMessage;   // 最后一条消息
    private String lastTime;      // 最后消息时间
    private long unread;          // 未读消息数
}
