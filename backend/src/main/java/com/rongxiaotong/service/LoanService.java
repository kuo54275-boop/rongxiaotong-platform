package com.rongxiaotong.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rongxiaotong.common.BizException;
import com.rongxiaotong.common.Roles;
import com.rongxiaotong.dto.ApplicationVO;
import com.rongxiaotong.dto.LoanApplyDTO;
import com.rongxiaotong.dto.PartnerMatchVO;
import com.rongxiaotong.entity.*;
import com.rongxiaotong.mapper.*;
import com.rongxiaotong.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 融资服务：贷款产品浏览、贷款申请（含智能匹配联合贷款人）、我的融资
 */
@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanProductMapper loanProductMapper;
    private final LoanApplicationMapper loanApplicationMapper;
    private final LoanPartnerMapper loanPartnerMapper;
    private final UserMapper userMapper;
    private final MatchService matchService;

    /** 上架的贷款产品列表 */
    public List<LoanProduct> productList() {
        return loanProductMapper.selectList(
                new LambdaQueryWrapper<LoanProduct>().eq(LoanProduct::getStatus, 1));
    }

    /** 贷款产品详情 */
    public LoanProduct productDetail(Long id) {
        LoanProduct p = loanProductMapper.selectById(id);
        if (p == null) {
            throw new BizException("贷款产品不存在");
        }
        return p;
    }

    /** 智能匹配联合贷款人 Top3（仅农户可用） */
    public List<PartnerMatchVO> matchPartners() {
        if (!UserContext.isRole(Roles.FARMER)) {
            throw new BizException("只有农户可以匹配联合贷款人");
        }
        return matchService.matchPartners(UserContext.getUserId());
    }

    /**
     * 提交贷款申请（含联合贷款人确认）
     * 注意 @Transactional：申请和联合人记录要么都成功，要么都失败
     */
    @Transactional
    public Long apply(LoanApplyDTO dto) {
        if (!UserContext.isRole(Roles.FARMER)) {
            throw new BizException("只有农户可以申请贷款");
        }
        // 1. 校验产品与金额
        LoanProduct product = loanProductMapper.selectById(dto.getProductId());
        if (product == null || product.getStatus() != 1) {
            throw new BizException("贷款产品不存在或已下架");
        }
        if (dto.getAmount() == null || dto.getAmount() < product.getMinAmount() || dto.getAmount() > product.getMaxAmount()) {
            throw new BizException("申请金额须在 " + product.getMinAmount() + " ~ " + product.getMaxAmount() + " 元之间");
        }
        if (dto.getTerm() == null || dto.getTerm() > product.getTermMonths()) {
            throw new BizException("申请期限不能超过 " + product.getTermMonths() + " 个月");
        }
        // 2. 创建贷款申请
        LoanApplication app = new LoanApplication();
        app.setFarmerId(UserContext.getUserId());
        app.setProductId(dto.getProductId());
        app.setAmount(dto.getAmount());
        app.setTerm(dto.getTerm());
        app.setPurpose(dto.getPurpose());
        app.setStatus("PENDING");
        app.setApplyTime(DateUtil.now());
        app.setBankId(product.getBankId());
        loanApplicationMapper.insert(app);
        // 3. 保存选中的联合贷款人
        if (dto.getPartnerId() != null) {
            LoanPartner lp = new LoanPartner();
            lp.setApplicationId(app.getId());
            lp.setPartnerFarmerId(dto.getPartnerId());
            lp.setScore(0.0);
            lp.setStatus(1);   // 1 = 已确认联合
            loanPartnerMapper.insert(lp);
        }
        return app.getId();
    }

    /** 我的融资申请列表（带产品名称和联合人姓名） */
    public List<ApplicationVO> myApplications() {
        List<LoanApplication> apps = loanApplicationMapper.selectList(
                new LambdaQueryWrapper<LoanApplication>()
                        .eq(LoanApplication::getFarmerId, UserContext.getUserId())
                        .orderByDesc(LoanApplication::getApplyTime));
        // 产品名称
        List<Long> productIds = apps.stream().map(LoanApplication::getProductId).distinct().toList();
        Map<Long, String> productNameMap = productIds.isEmpty() ? Map.of()
                : loanProductMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(LoanProduct::getId, LoanProduct::getName));
        // 联合人姓名
        List<Long> appIds = apps.stream().map(LoanApplication::getId).toList();
        Map<Long, String> partnerNameMap = Map.of();
        if (!appIds.isEmpty()) {
            List<LoanPartner> partners = loanPartnerMapper.selectList(
                    new LambdaQueryWrapper<LoanPartner>().in(LoanPartner::getApplicationId, appIds));
            if (!partners.isEmpty()) {
                Map<Long, User> userMap = userMapper.selectBatchIds(
                                partners.stream().map(LoanPartner::getPartnerFarmerId).distinct().toList())
                        .stream().collect(Collectors.toMap(User::getId, u -> u));
                partnerNameMap = partners.stream().collect(Collectors.toMap(
                        LoanPartner::getApplicationId,
                        lp -> userMap.getOrDefault(lp.getPartnerFarmerId(), new User()).getRealName(),
                        (a, b) -> a));
            }
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
            vo.setProductName(productNameMap.getOrDefault(a.getProductId(), "未知产品"));
            vo.setPartnerName(finalPartnerNameMap.get(a.getId()));
            return vo;
        }).toList();
    }
}
