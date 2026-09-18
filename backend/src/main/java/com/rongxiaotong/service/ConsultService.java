package com.rongxiaotong.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rongxiaotong.common.BizException;
import com.rongxiaotong.common.Roles;
import com.rongxiaotong.dto.ContactVO;
import com.rongxiaotong.entity.Appointment;
import com.rongxiaotong.entity.ConsultMessage;
import com.rongxiaotong.entity.Question;
import com.rongxiaotong.entity.User;
import com.rongxiaotong.mapper.AppointmentMapper;
import com.rongxiaotong.mapper.ConsultMessageMapper;
import com.rongxiaotong.mapper.QuestionMapper;
import com.rongxiaotong.mapper.UserMapper;
import com.rongxiaotong.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 在线咨询服务：农户与专家的消息式对话
 * 前端每 3 秒轮询一次消息列表（简单可靠，无需 WebSocket）
 */
@Service
@RequiredArgsConstructor
public class ConsultService {

    private final ConsultMessageMapper consultMessageMapper;
    private final UserMapper userMapper;
    private final AppointmentMapper appointmentMapper;
    private final QuestionMapper questionMapper;
    private final NotificationService notificationService;

    /** 发送一条消息 */
    public Long send(Long receiverId, String content) {
        if (receiverId == null || StrUtil.isBlank(content)) {
            throw new BizException("消息内容不能为空");
        }
        if (receiverId.equals(UserContext.getUserId())) {
            throw new BizException("不能给自己发消息");
        }
        ConsultMessage msg = new ConsultMessage();
        msg.setSenderId(UserContext.getUserId());
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setIsRead(0);
        msg.setCreateTime(DateUtil.now());
        consultMessageMapper.insert(msg);
        return msg.getId();
    }

    /**
     * 我与某人的完整对话（按时间正序），并把对方发来的消息标记为已读
     */
    public List<ConsultMessage> listWith(Long withUserId) {
        Long me = UserContext.getUserId();
        List<ConsultMessage> list = consultMessageMapper.selectList(
                new LambdaQueryWrapper<ConsultMessage>()
                        .and(w -> w.and(x -> x.eq(ConsultMessage::getSenderId, me)
                                .eq(ConsultMessage::getReceiverId, withUserId))
                                .or(x -> x.eq(ConsultMessage::getSenderId, withUserId)
                                        .eq(ConsultMessage::getReceiverId, me)))
                        .orderByAsc(ConsultMessage::getCreateTime));
        // 对方发给我的消息标记已读
        for (ConsultMessage m : list) {
            if (m.getReceiverId().equals(me) && m.getIsRead() == 0) {
                m.setIsRead(1);
                consultMessageMapper.updateById(m);
            }
        }
        return list;
    }

    /**
     * 通知统计（顶部铃铛红点用）：未读消息数 + 专家的待处理预约数和待回答问题数
     */
    public com.rongxiaotong.dto.ConsultStatsVO stats() {
        Long me = UserContext.getUserId();
        com.rongxiaotong.dto.ConsultStatsVO vo = new com.rongxiaotong.dto.ConsultStatsVO();
        // 未读消息
        vo.setUnreadMessages(consultMessageMapper.selectCount(
                new LambdaQueryWrapper<ConsultMessage>()
                        .eq(ConsultMessage::getReceiverId, me)
                        .eq(ConsultMessage::getIsRead, 0)));
        // 未读系统通知
        vo.setUnreadNotifications(notificationService.unreadCount(me));
        // 专家：待处理预约 + 待回答问题
        if (Roles.EXPERT.equals(UserContext.getRole())) {
            vo.setPendingAppointments(appointmentMapper.selectCount(
                    new LambdaQueryWrapper<Appointment>()
                            .eq(Appointment::getExpertId, me)
                            .eq(Appointment::getStatus, "PENDING")));
            vo.setTodoQuestions(questionMapper.selectCount(
                    new LambdaQueryWrapper<Question>().eq(Question::getStatus, 0)));
        }
        return vo;
    }

    /**
     * 我的会话列表：所有和我聊过天的人（按最后一条消息时间倒序），带未读数
     */
    public List<ContactVO> contacts() {
        Long me = UserContext.getUserId();
        List<ConsultMessage> all = consultMessageMapper.selectList(
                new LambdaQueryWrapper<ConsultMessage>()
                        .and(w -> w.eq(ConsultMessage::getSenderId, me)
                                .or().eq(ConsultMessage::getReceiverId, me))
                        .orderByAsc(ConsultMessage::getCreateTime));
        // 按对方分组统计
        Map<Long, List<ConsultMessage>> grouped = new HashMap<>();
        for (ConsultMessage m : all) {
            Long other = m.getSenderId().equals(me) ? m.getReceiverId() : m.getSenderId();
            grouped.computeIfAbsent(other, k -> new ArrayList<>()).add(m);
        }
        // 组装联系人列表
        List<ContactVO> result = new ArrayList<>();
        for (Map.Entry<Long, List<ConsultMessage>> entry : grouped.entrySet()) {
            User other = userMapper.selectById(entry.getKey());
            if (other == null) {
                continue;
            }
            List<ConsultMessage> msgs = entry.getValue();
            ConsultMessage last = msgs.get(msgs.size() - 1);
            long unread = msgs.stream()
                    .filter(m -> m.getReceiverId().equals(me) && m.getIsRead() == 0).count();
            ContactVO vo = new ContactVO();
            vo.setUserId(other.getId());
            vo.setRealName(other.getRealName());
            vo.setAvatar(other.getAvatar());
            vo.setLastMessage(last.getContent());
            vo.setLastTime(last.getCreateTime());
            vo.setUnread(unread);
            result.add(vo);
        }
        result.sort(Comparator.comparing(ContactVO::getLastTime).reversed());
        return result;
    }
}
