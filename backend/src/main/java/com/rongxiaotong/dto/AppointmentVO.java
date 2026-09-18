package com.rongxiaotong.dto;

import com.rongxiaotong.entity.Appointment;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 预约展示对象：预约信息 + 农户姓名电话 + 专家姓名职称
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppointmentVO extends Appointment {

    private String farmerName;     // 预约农户姓名
    private String farmerPhone;    // 预约农户电话
    private String expertName;     // 专家姓名
    private String expertTitle;    // 专家职称
}
