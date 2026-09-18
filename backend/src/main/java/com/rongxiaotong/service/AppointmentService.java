package com.rongxiaotong.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rongxiaotong.common.BizException;
import com.rongxiaotong.common.Roles;
import com.rongxiaotong.dto.AppointmentVO;
import com.rongxiaotong.entity.Appointment;
import com.rongxiaotong.entity.ExpertProfile;
import com.rongxiaotong.entity.User;
import com.rongxiaotong.mapper.AppointmentMapper;
import com.rongxiaotong.mapper.ExpertProfileMapper;
import com.rongxiaotong.mapper.UserMapper;
import com.rongxiaotong.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 预约服务：农户预约专家，专家处理预约
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final UserMapper userMapper;
    private final ExpertProfileMapper expertProfileMapper;
    private final NotificationService notificationService;

    /** 农户：提交预约 */
    public Long create(Appointment appointment) {
        if (!UserContext.isRole(Roles.FARMER)) {
            throw new BizException("只有农户可以预约专家");
        }
        if (appointment.getExpertId() == null) {
            throw new BizException("请选择要预约的专家");
        }
        if (StrUtil.isBlank(appointment.getTopic())) {
            throw new BizException("请填写预约主题");
        }
        appointment.setFarmerId(UserContext.getUserId());
        appointment.setStatus("PENDING");
        appointment.setCreateTime(DateUtil.now());
        appointmentMapper.insert(appointment);
        return appointment.getId();
    }

    /** 农户：我的预约（附专家姓名职称） */
    public List<AppointmentVO> myAppointments() {
        List<Appointment> list = appointmentMapper.selectList(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getFarmerId, UserContext.getUserId())
                        .orderByDesc(Appointment::getCreateTime));
        Map<Long, User> expertMap = userMap(list.stream().map(Appointment::getExpertId).toList());
        Map<Long, ExpertProfile> profileMap = profileMap(expertMap.keySet().stream().toList());
        return list.stream().map(a -> {
            AppointmentVO vo = new AppointmentVO();
            copy(a, vo);
            User e = expertMap.get(a.getExpertId());
            ExpertProfile ep = profileMap.get(a.getExpertId());
            if (e != null) {
                vo.setExpertName(e.getRealName());
            }
            if (ep != null) {
                vo.setExpertTitle(ep.getTitle());
            }
            return vo;
        }).toList();
    }

    /** 专家：收到的预约（附农户姓名电话） */
    public List<AppointmentVO> expertAppointments() {
        if (!UserContext.isRole(Roles.EXPERT)) {
            throw new BizException("只有技术专家可以查看预约");
        }
        List<Appointment> list = appointmentMapper.selectList(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getExpertId, UserContext.getUserId())
                        .orderByDesc(Appointment::getCreateTime));
        Map<Long, User> farmerMap = userMap(list.stream().map(Appointment::getFarmerId).toList());
        return list.stream().map(a -> {
            AppointmentVO vo = new AppointmentVO();
            copy(a, vo);
            User f = farmerMap.get(a.getFarmerId());
            if (f != null) {
                vo.setFarmerName(f.getRealName());
                vo.setFarmerPhone(f.getPhone());
            }
            return vo;
        }).toList();
    }

    /** 专家：处理预约（确认 / 完成 / 取消） */
    public void updateStatus(Long id, String status) {
        if (!UserContext.isRole(Roles.EXPERT)) {
            throw new BizException("只有技术专家可以处理预约");
        }
        if (!StrUtil.equalsAny(status, "CONFIRMED", "DONE", "CANCELLED")) {
            throw new BizException("无效的预约状态");
        }
        Appointment a = appointmentMapper.selectById(id);
        if (a == null || !a.getExpertId().equals(UserContext.getUserId())) {
            throw new BizException("预约不存在");
        }
        a.setStatus(status);
        appointmentMapper.updateById(a);
        // 通知农户预约状态变化
        String statusText = "CONFIRMED".equals(status) ? "专家已确认您的预约「" + a.getTopic() + "」，请按时等待专家上门"
                : "DONE".equals(status) ? "您的预约「" + a.getTopic() + "」已完成，感谢使用"
                : "您的预约「" + a.getTopic() + "」已被专家取消";
        notificationService.send(a.getFarmerId(), "APPOINTMENT_HANDLED", "📅 预约状态更新", statusText, a.getId());
        // 完成一次预约，专家服务次数 +1
        if ("DONE".equals(status)) {
            ExpertProfile ep = expertProfileMapper.selectOne(
                    new LambdaQueryWrapper<ExpertProfile>().eq(ExpertProfile::getUserId, UserContext.getUserId()));
            if (ep != null) {
                ep.setServiceCount(ep.getServiceCount() == null ? 1 : ep.getServiceCount() + 1);
                expertProfileMapper.updateById(ep);
            }
        }
    }

    /** 农户：取消自己的预约 */
    public void cancel(Long id) {
        Appointment a = appointmentMapper.selectById(id);
        if (a == null || !a.getFarmerId().equals(UserContext.getUserId())) {
            throw new BizException("预约不存在");
        }
        a.setStatus("CANCELLED");
        appointmentMapper.updateById(a);
    }

    private void copy(Appointment src, AppointmentVO vo) {
        vo.setId(src.getId());
        vo.setFarmerId(src.getFarmerId());
        vo.setExpertId(src.getExpertId());
        vo.setTopic(src.getTopic());
        vo.setDescription(src.getDescription());
        vo.setAppointTime(src.getAppointTime());
        vo.setPhone(src.getPhone());
        vo.setStatus(src.getStatus());
        vo.setCreateTime(src.getCreateTime());
    }

    private Map<Long, User> userMap(List<Long> ids) {
        List<Long> distinctIds = ids.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (distinctIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(distinctIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
    }

    private Map<Long, ExpertProfile> profileMap(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return expertProfileMapper.selectList(
                        new LambdaQueryWrapper<ExpertProfile>().in(ExpertProfile::getUserId, userIds))
                .stream().collect(Collectors.toMap(ExpertProfile::getUserId, p -> p));
    }
}
