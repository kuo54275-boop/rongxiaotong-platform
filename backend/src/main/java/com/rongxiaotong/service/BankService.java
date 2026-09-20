package com.rongxiaotong.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rongxiaotong.common.BizException;
import com.rongxiaotong.common.Roles;
import com.rongxiaotong.dto.ApplicationVO;
import com.rongxiaotong.dto.FarmerMatchVO;
import com.rongxiaotong.entity.*;
import com.rongxiaotong.mapper.*;
import com.rongxiaotong.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 银行侧融资管理服务：智能匹配农户、贷款审批、工作台统计
 */
@Service
@RequiredArgsConstructor
public class BankService {

    private final MatchService matchService;
    private final LoanApplicationMapper loanApplicationMapper;
    private final LoanProductMapper loanProductMapper;
    private final LoanPartnerMapper loanPartnerMapper;
    private final MatchRecordMapper matchRecordMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    // ==================== 智能匹配农户 ====================

    /**
     * 为指定贷款产品匹配农户 Top10，并保存匹配记录
     */
    public List<FarmerMatchVO> matchFarmers(Long productId) {
        requireBank();
        LoanProduct product = loanProductMapper.selectById(productId);
        if (product == null) {
            throw new BizException("贷款产品不存在");
        }
        List<FarmerMatchVO> list = matchService.matchFarmers(productId);
        Long bankId = UserContext.getUserId();
        for (FarmerMatchVO vo : list) {
            // 同银行同产品同农户只保留一条推荐记录（已联系的不重复插）
            Long exist = matchRecordMapper.selectCount(new LambdaQueryWrapper<MatchRecord>()
                    .eq(MatchRecord::getBankId, bankId)
                    .eq(MatchRecord::getFarmerId, vo.getUserId())
                    .eq(MatchRecord::getProductId, productId));
            if (exist == 0) {
                MatchRecord mr = new MatchRecord();
                mr.setBankId(bankId);
                mr.setFarmerId(vo.getUserId());
                mr.setProductId(productId);
                mr.setScore(vo.getScore());
                mr.setStatus(0);
                mr.setCreateTime(DateUtil.now());
                matchRecordMapper.insert(mr);
                vo.setRecordId(mr.getId());
            }
        }
        return list;
    }

    /** 我的匹配记录（含农户和产品名称） */
    public List<FarmerMatchVO> matchRecords() {
        requireBank();
        List<MatchRecord> records = matchRecordMapper.selectList(
                new LambdaQueryWrapper<MatchRecord>()
                        .eq(MatchRecord::getBankId, UserContext.getUserId())
                        .orderByDesc(MatchRecord::getCreateTime));
        if (records.isEmpty()) {
            return List.of();
        }
        Map<Long, User> farmerMap = userMapper.selectBatchIds(
                        records.stream().map(MatchRecord::getFarmerId).distinct().toList())
                .stream().collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, LoanProduct> productMap = loanProductMapper.selectBatchIds(
                        records.stream().map(MatchRecord::getProductId).distinct().toList())
                .stream().collect(Collectors.toMap(LoanProduct::getId, p -> p));
        return records.stream().map(r -> {
            FarmerMatchVO vo = new FarmerMatchVO();
            vo.setRecordId(r.getId());
            vo.setUserId(r.getFarmerId());
            User f = farmerMap.get(r.getFarmerId());
            if (f != null) {
                vo.setRealName(f.getRealName());
                vo.setPhone(f.getPhone());
            }
            LoanProduct p = productMap.get(r.getProductId());
            vo.setReason(p == null ? "" : p.getName());
            vo.setScore(r.getScore());
            vo.setAddress(r.getStatus() == 1 ? "已联系" : "已推荐");
            return vo;
        }).toList();
    }

    /** 标记已联系 */
    public void markContacted(Long recordId) {
        requireBank();
        MatchRecord mr = matchRecordMapper.selectById(recordId);
        if (mr == null || !mr.getBankId().equals(UserContext.getUserId())) {
            throw new BizException("匹配记录不存在");
        }
        mr.setStatus(1);
        matchRecordMapper.updateById(mr);
    }

    // ==================== 贷款审批 ====================

    /** 贷款申请列表（按状态筛选，默认全部） */
    public List<ApplicationVO> applications(String status) {
        requireBank();
        LambdaQueryWrapper<LoanApplication> qw = new LambdaQueryWrapper<LoanApplication>()
                .eq(StrUtil.isNotBlank(status), LoanApplication::getStatus, status)
                .orderByDesc(LoanApplication::getApplyTime);
        List<LoanApplication> apps = loanApplicationMapper.selectList(qw);
        return fillApplicationVO(apps);
    }

    /** 审批通过 */
    public void approve(Long id) {
        LoanApplication app = mustPendingApplication(id);
        app.setStatus("APPROVED");
        app.setApproveTime(DateUtil.now());
        loanApplicationMapper.updateById(app);
        notificationService.send(app.getFarmerId(), "LOAN_APPROVED", "🎉 贷款审批通过",
                "您的贷款申请（" + app.getAmount() + " 元）已审批通过，请留意放款", app.getId());
    }

    /** 审批拒绝 */
    public void reject(Long id) {
        LoanApplication app = mustPendingApplication(id);
        app.setStatus("REJECTED");
        app.setApproveTime(DateUtil.now());
        loanApplicationMapper.updateById(app);
        notificationService.send(app.getFarmerId(), "LOAN_REJECTED", "📋 贷款审批结果",
                "很抱歉，您的贷款申请（" + app.getAmount() + " 元）未通过审批，可联系银行咨询详情", app.getId());
    }

    private LoanApplication mustPendingApplication(Long id) {
        requireBank();
        LoanApplication app = loanApplicationMapper.selectById(id);
        if (app == null) {
            throw new BizException("申请不存在");
        }
        if (!"PENDING".equals(app.getStatus())) {
            throw new BizException("该申请已处理过");
        }
        return app;
    }

    /** 组装申请展示对象（含农户和产品信息） */
    private List<ApplicationVO> fillApplicationVO(List<LoanApplication> apps) {
        if (apps.isEmpty()) {
            return List.of();
        }
        Map<Long, User> farmerMap = userMap(apps.stream().map(LoanApplication::getFarmerId).toList());
        Map<Long, LoanProduct> productMap = loanProductMapper.selectBatchIds(
                        apps.stream().map(LoanApplication::getProductId).distinct().toList())
                .stream().collect(Collectors.toMap(LoanProduct::getId, p -> p));
        // 联合人姓名
        Map<Long, String> partnerNameMap = Map.of();
        List<LoanPartner> partners = loanPartnerMapper.selectList(new LambdaQueryWrapper<LoanPartner>()
                .in(LoanPartner::getApplicationId, apps.stream().map(LoanApplication::getId).toList()));
        if (!partners.isEmpty()) {
            Map<Long, User> partnerUserMap = userMap(
                    partners.stream().map(LoanPartner::getPartnerFarmerId).distinct().toList());
            partnerNameMap = partners.stream().collect(Collectors.toMap(
                    LoanPartner::getApplicationId,
                    lp -> partnerUserMap.getOrDefault(lp.getPartnerFarmerId(), new User()).getRealName(),
                    (a, b) -> a));
        }
        Map<Long, String> finalPartnerNameMap = partnerNameMap;
        return apps.stream().map(a -> {
            ApplicationVO vo = new ApplicationVO();
            vo.setId(a.getId());
            vo.setFarmerId(a.getFarmerId());
            vo.setProductId(a.getProductId());
            vo.setAmount(a.getAmount());
            vo.setTerm(a.getTerm());
            vo.setPurpose(a.getPurpose());
            vo.setStatus(a.getStatus());
            vo.setApplyTime(a.getApplyTime());
            vo.setApproveTime(a.getApproveTime());
            vo.setBankId(a.getBankId());
            User f = farmerMap.get(a.getFarmerId());
            if (f != null) {
                vo.setFarmerName(f.getRealName());
                vo.setFarmerPhone(f.getPhone());
            }
            LoanProduct p = productMap.get(a.getProductId());
            vo.setProductName(p == null ? "" : p.getName());
            vo.setPartnerName(finalPartnerNameMap.get(a.getId()));
            return vo;
        }).toList();
    }

    // ==================== 工作台统计 ====================

    /** 工作台统计数字 */
    public Map<String, Object> stats() {
        requireBank();
        Long bankId = UserContext.getUserId();
        return Map.of(
                "pendingCount", loanApplicationMapper.selectCount(new LambdaQueryWrapper<LoanApplication>()
                        .eq(LoanApplication::getBankId, bankId).eq(LoanApplication::getStatus, "PENDING")),
                "approvedCount", loanApplicationMapper.selectCount(new LambdaQueryWrapper<LoanApplication>()
                        .eq(LoanApplication::getBankId, bankId).eq(LoanApplication::getStatus, "APPROVED")),
                "rejectedCount", loanApplicationMapper.selectCount(new LambdaQueryWrapper<LoanApplication>()
                        .eq(LoanApplication::getBankId, bankId).eq(LoanApplication::getStatus, "REJECTED")),
                "matchCount", matchRecordMapper.selectCount(new LambdaQueryWrapper<MatchRecord>()
                        .eq(MatchRecord::getBankId, bankId)),
                "productCount", loanProductMapper.selectCount(new LambdaQueryWrapper<LoanProduct>()
                        .eq(LoanProduct::getBankId, bankId).eq(LoanProduct::getStatus, 1))
        );
    }

    private Map<Long, User> userMap(List<Long> ids) {
        List<Long> distinctIds = ids.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (distinctIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(distinctIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
    }

    private void requireBank() {
        if (!UserContext.isRole(Roles.BANK)) {
            throw new BizException("只有银行可以使用融资管理后台");
        }
    }
}
