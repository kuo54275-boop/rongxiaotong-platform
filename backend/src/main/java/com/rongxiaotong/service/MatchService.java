package com.rongxiaotong.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rongxiaotong.common.Roles;
import com.rongxiaotong.dto.PartnerMatchVO;
import com.rongxiaotong.entity.FarmerProfile;
import com.rongxiaotong.entity.LoanApplication;
import com.rongxiaotong.entity.LoanPartner;
import com.rongxiaotong.entity.User;
import com.rongxiaotong.mapper.FarmerProfileMapper;
import com.rongxiaotong.mapper.LoanApplicationMapper;
import com.rongxiaotong.mapper.LoanPartnerMapper;
import com.rongxiaotong.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 智能匹配算法服务（本项目核心亮点，答辩重点讲解）
 *
 * 算法说明——联合贷款人匹配：
 * 农户申请贷款时，从其他农户中推荐适合"联合贷款"的伙伴，
 * 采用加权打分法，四个维度：
 *   1. 信用分    （权重 40%）——信用越好越值得信赖
 *   2. 还款能力  （权重 30%）——由种植面积和信用分综合估算
 *   3. 地域相近  （权重 20%）——同市/同省更便于合作与监督
 *   4. 历史合作  （权重 10%）——之前成功联合过可加分
 * 最后按总分排序，取 Top3 推荐给申请农户选择。
 */
@Service
@RequiredArgsConstructor
public class MatchService {

    private final UserMapper userMapper;
    private final FarmerProfileMapper farmerProfileMapper;
    private final LoanPartnerMapper loanPartnerMapper;
    private final LoanApplicationMapper loanApplicationMapper;

    /**
     * 为指定农户匹配 Top3 联合贷款人
     */
    public List<PartnerMatchVO> matchPartners(Long farmerId) {
        // 1. 候选池：除自己外的所有农户
        List<User> candidates = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getRole, Roles.FARMER)
                        .ne(User::getId, farmerId));
        if (candidates.isEmpty()) {
            return List.of();
        }

        // 2. 自己的档案（用于地域比较）
        FarmerProfile myProfile = farmerProfileMapper.selectOne(
                new LambdaQueryWrapper<FarmerProfile>().eq(FarmerProfile::getUserId, farmerId));
        String myCity = extractCity(myProfile == null ? "" : myProfile.getAddress());

        // 3. 历史合作过的农户 id（之前确认过联合贷款且申请已通过）
        List<Long> historyPartnerIds = new ArrayList<>();
        List<LoanPartner> myPartners = loanPartnerMapper.selectList(
                new LambdaQueryWrapper<LoanPartner>().eq(LoanPartner::getPartnerFarmerId, farmerId));
        for (LoanPartner lp : myPartners) {
            LoanApplication app = loanApplicationMapper.selectById(lp.getApplicationId());
            if (app != null && "APPROVED".equals(app.getStatus())) {
                historyPartnerIds.add(app.getFarmerId());
            }
        }

        // 4. 逐个打分
        List<PartnerMatchVO> result = new ArrayList<>();
        for (User c : candidates) {
            FarmerProfile fp = farmerProfileMapper.selectOne(
                    new LambdaQueryWrapper<FarmerProfile>().eq(FarmerProfile::getUserId, c.getId()));
            if (fp == null) {
                continue;   // 没有档案的农户不参与匹配
            }

            double credit = nvl(fp.getCreditScore(), 80);
            double landArea = nvl(fp.getLandArea(), 0.0);

            // 维度一：信用分（40%），信用分 0-100 直接作为得分
            double creditPoint = Math.min(credit, 100);

            // 维度二：还款能力（30%），由种植面积(0-200亩)和信用分各占一半估算
            double landPoint = Math.min(landArea / 200.0 * 100, 100);
            double abilityPoint = landPoint * 0.5 + creditPoint * 0.5;

            // 维度三：地域相近（20%），同市 100 / 同省 60 / 其他 20
            String partnerCity = extractCity(fp.getAddress());
            double regionPoint = 20;
            if (StrUtil.isNotBlank(myCity) && myCity.equals(partnerCity)) {
                regionPoint = 100;
            } else if (StrUtil.isNotBlank(myCity) && isSameProvince(myCity, partnerCity)) {
                regionPoint = 60;
            }

            // 维度四：历史合作（10%），合作过 100，否则 0
            double historyPoint = historyPartnerIds.contains(c.getId()) ? 100 : 0;

            // 5. 加权总分
            double score = creditPoint * 0.4 + abilityPoint * 0.3 + regionPoint * 0.2 + historyPoint * 0.1;
            score = Math.round(score * 10) / 10.0;   // 保留 1 位小数

            PartnerMatchVO vo = new PartnerMatchVO();
            vo.setUserId(c.getId());
            vo.setRealName(c.getRealName());
            vo.setAddress(fp.getAddress());
            vo.setCrop(fp.getCrop());
            vo.setLandArea(landArea);
            vo.setScore(score);
            vo.setCreditPoint(Math.round(creditPoint * 10) / 10.0);
            vo.setAbilityPoint(Math.round(abilityPoint * 10) / 10.0);
            vo.setRegionPoint(regionPoint);
            vo.setHistoryPoint(historyPoint);
            vo.setReason(buildReason(vo));
            result.add(vo);
        }

        // 6. 按总分倒序取 Top3
        result.sort(Comparator.comparing(PartnerMatchVO::getScore).reversed());
        return result.size() > 3 ? result.subList(0, 3) : result;
    }

    /** 生成一句话推荐理由（答辩时展示算法可解释性） */
    private String buildReason(PartnerMatchVO vo) {
        StringBuilder sb = new StringBuilder();
        if (vo.getCreditPoint() >= 90) {
            sb.append("信用优秀");
        } else if (vo.getCreditPoint() >= 80) {
            sb.append("信用良好");
        } else {
            sb.append("信用一般");
        }
        sb.append("，");
        if (vo.getAbilityPoint() >= 80) {
            sb.append("还款能力强");
        } else {
            sb.append("还款能力中等");
        }
        if (vo.getRegionPoint() == 100) {
            sb.append("，与你同市");
        } else if (vo.getRegionPoint() == 60) {
            sb.append("，与你同省");
        }
        if (vo.getHistoryPoint() > 0) {
            sb.append("，曾成功联合");
        }
        return sb.toString();
    }

    /** 从地址中提取城市："河南省周口市沈丘县" -> "河南省周口市" */
    private String extractCity(String address) {
        if (StrUtil.isBlank(address)) {
            return "";
        }
        int idx = address.indexOf('市');
        if (idx > 0 && idx + 1 < address.length()) {
            return address.substring(0, idx + 1);
        }
        return address;
    }

    /** 判断两个城市是否同省："河南省周口市" 和 "河南省驻马店市" -> true */
    private boolean isSameProvince(String city1, String city2) {
        if (StrUtil.isBlank(city1) || StrUtil.isBlank(city2)) {
            return false;
        }
        return city1.charAt(0) == city2.charAt(0) && city1.length() > 1 && city2.length() > 1
                && city1.substring(0, 2).equals(city2.substring(0, 2));
    }

    private double nvl(Integer v, double def) {
        return v == null ? def : v;
    }

    private double nvl(Double v, double def) {
        return v == null ? def : v;
    }
}
