package com.rongxiaotong.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rongxiaotong.entity.Notification;
import com.rongxiaotong.mapper.NotificationMapper;
import com.rongxiaotong.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统通知服务：业务事件发生后调用 send() 生成通知
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;

    /** 给指定用户发一条通知 */
    public void send(Long userId, String type, String title, String content, Long relatedId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setRelatedId(relatedId);
        n.setIsRead(0);
        n.setCreateTime(DateUtil.now());
        notificationMapper.insert(n);
    }

    /** 我的通知列表（最近 50 条） */
    public List<Notification> myList() {
        return notificationMapper.selectList(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, UserContext.getUserId())
                        .orderByDesc(Notification::getCreateTime)
                        .last("LIMIT 50"));
    }

    /** 我的未读通知数 */
    public long unreadCount(Long userId) {
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0));
    }

    /** 标记一条已读 */
    public void markRead(Long id) {
        Notification n = notificationMapper.selectById(id);
        if (n == null || !n.getUserId().equals(UserContext.getUserId())) {
            return;
        }
        n.setIsRead(1);
        notificationMapper.updateById(n);
    }

    /** 全部标记已读 */
    public void markAllRead() {
        List<Notification> list = notificationMapper.selectList(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, UserContext.getUserId())
                        .eq(Notification::getIsRead, 0));
        for (Notification n : list) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
    }
}
