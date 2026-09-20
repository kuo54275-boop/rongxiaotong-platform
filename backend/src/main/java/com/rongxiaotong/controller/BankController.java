package com.rongxiaotong.controller;

import com.rongxiaotong.common.Result;
import com.rongxiaotong.dto.ApplicationVO;
import com.rongxiaotong.dto.FarmerMatchVO;
import com.rongxiaotong.service.BankService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 银行侧融资管理接口（仅银行角色可用）
 */
@RestController
@RequestMapping("/bank")
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    /** 智能匹配农户（为指定产品推荐 Top10） */
    @GetMapping("/match/farmers")
    public Result<List<FarmerMatchVO>> matchFarmers(@RequestParam Long productId) {
        return Result.ok("匹配完成", bankService.matchFarmers(productId));
    }

    /** 我的匹配记录 */
    @GetMapping("/match/records")
    public Result<List<FarmerMatchVO>> matchRecords() {
        return Result.ok(bankService.matchRecords());
    }

    /** 标记已联系 */
    @PutMapping("/match/{recordId}/contact")
    public Result<Void> markContacted(@PathVariable Long recordId) {
        bankService.markContacted(recordId);
        return Result.ok("已标记联系", null);
    }

    /** 贷款申请列表（status 可选：PENDING / APPROVED / REJECTED） */
    @GetMapping("/applications")
    public Result<List<ApplicationVO>> applications(@RequestParam(required = false) String status) {
        return Result.ok(bankService.applications(status));
    }

    /** 审批通过 */
    @PutMapping("/application/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        bankService.approve(id);
        return Result.ok("已通过审批", null);
    }

    /** 审批拒绝 */
    @PutMapping("/application/{id}/reject")
    public Result<Void> reject(@PathVariable Long id) {
        bankService.reject(id);
        return Result.ok("已拒绝该申请", null);
    }

    /** 工作台统计 */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.ok(bankService.stats());
    }
}
