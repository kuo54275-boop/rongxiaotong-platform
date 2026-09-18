package com.rongxiaotong.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rongxiaotong.common.BizException;
import com.rongxiaotong.dto.ContactVO;
import com.rongxiaotong.entity.ConsultMessage;
import com.rongxiaotong.entity.User;
import com.rongxiaotong.mapper.ConsultMessageMapper;
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
