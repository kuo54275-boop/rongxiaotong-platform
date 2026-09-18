package com.rongxiaotong.controller;

import com.rongxiaotong.common.Result;
import com.rongxiaotong.dto.ApplicationVO;
import com.rongxiaotong.dto.LoanApplyDTO;
import com.rongxiaotong.dto.PartnerMatchVO;
import com.rongxiaotong.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 融资服务接口（申请、匹配、我的融资需要登录；产品列表是公开的，在 PublicController）
 */
@RestController
@RequestMapping("/loan")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    /** 智能匹配联合贷款人 Top3 */
    @GetMapping("/partner/match")
    public Result<List<PartnerMatchVO>> matchPartners() {
        return Result.ok(loanService.matchPartners());
    }

    /** 提交贷款申请 */
    @PostMapping("/apply")
    public Result<Long> apply(@RequestBody LoanApplyDTO dto) {
        return Result.ok("申请提交成功，等待银行审批", loanService.apply(dto));
    }

    /** 我的融资申请列表 */
    @GetMapping("/my")
    public Result<List<ApplicationVO>> myApplications() {
        return Result.ok(loanService.myApplications());
    }
}
