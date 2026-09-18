package com.rongxiaotong.controller;

import com.rongxiaotong.common.Result;
import com.rongxiaotong.dto.AppointmentVO;
import com.rongxiaotong.entity.Appointment;
import com.rongxiaotong.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预约接口：农户预约专家，专家处理预约
 */
@RestController
@RequestMapping("/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /** 农户：提交预约 */
    @PostMapping("/create")
    public Result<Long> create(@RequestBody Appointment appointment) {
        return Result.ok("预约提交成功，等待专家确认", appointmentService.create(appointment));
    }

    /** 农户：我的预约 */
    @GetMapping("/my")
    public Result<List<AppointmentVO>> myAppointments() {
        return Result.ok(appointmentService.myAppointments());
    }

    /** 专家：收到的预约 */
    @GetMapping("/expert")
    public Result<List<AppointmentVO>> expertAppointments() {
        return Result.ok(appointmentService.expertAppointments());
    }

    /** 专家：处理预约（CONFIRMED 确认 / DONE 完成 / CANCELLED 取消） */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        appointmentService.updateStatus(id, status);
        return Result.ok("处理成功", null);
    }

    /** 农户：取消预约 */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        appointmentService.cancel(id);
        return Result.ok("已取消预约", null);
    }
}
