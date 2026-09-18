package com.rongxiaotong.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rongxiaotong.common.BizException;
import com.rongxiaotong.common.PageResult;
import com.rongxiaotong.common.Roles;
import com.rongxiaotong.dto.DemandVO;
import com.rongxiaotong.entity.Demand;
import com.rongxiaotong.entity.User;
import com.rongxiaotong.mapper.DemandMapper;
import com.rongxiaotong.mapper.UserMapper;
import com.rongxiaotong.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 求购需求服务：买家发布采购需求，农户查看
 */
@Service
@RequiredArgsConstructor
public class DemandService {

    private final DemandMapper demandMapper;
    private final UserMapper userMapper;

    /** 求购大厅（有效需求，分页 + 分类） */
    public PageResult<DemandVO> pageDemands(long page, long size, String category) {
        LambdaQueryWrapper<Demand> qw = new LambdaQueryWrapper<Demand>()
                .eq(Demand::getStatus, 1)
                .eq(StrUtil.isNotBlank(category), Demand::getCategory, category)
                .orderByDesc(Demand::getCreateTime);
        Page<Demand> p = demandMapper.selectPage(new Page<>(page, size), qw);
        List<DemandVO> list = fillDemandVO(p.getRecords());
        return PageResult.of(list, p.getTotal(), p.getCurrent(), p.getSize());
    }

    /** 需求详情 */
    public DemandVO demandDetail(Long id) {
        Demand d = demandMapper.selectById(id);
        if (d == null) {
            throw new BizException("需求不存在");
        }
        return fillDemandVO(List.of(d)).get(0);
    }

    /** 买家：我的需求 */
    public List<DemandVO> myDemands() {
        List<Demand> list = demandMapper.selectList(
                new LambdaQueryWrapper<Demand>()
                        .eq(Demand::getBuyerId, UserContext.getUserId())
                        .orderByDesc(Demand::getCreateTime));
        return fillDemandVO(list);
    }

    /** 买家：发布 / 修改需求 */
    public Long saveDemand(Demand demand) {
        if (!UserContext.isRole(Roles.BUYER)) {
            throw new BizException("只有买家可以发布求购需求");
        }
        if (StrUtil.isBlank(demand.getTitle()) || demand.getQuantity() == null) {
            throw new BizException("请填写需求标题和数量");
        }
        if (demand.getId() == null) {
            demand.setBuyerId(UserContext.getUserId());
            demand.setStatus(1);
            demand.setCreateTime(DateUtil.now());
            demandMapper.insert(demand);
        } else {
            Demand old = demandMapper.selectById(demand.getId());
            if (old == null || !old.getBuyerId().equals(UserContext.getUserId())) {
                throw new BizException("只能修改自己的需求");
            }
            demand.setBuyerId(old.getBuyerId());
            demand.setStatus(old.getStatus());
            demand.setCreateTime(old.getCreateTime());
            demandMapper.updateById(demand);
        }
        return demand.getId();
    }

    /** 买家：结束 / 重新开启需求 */
    public void updateStatus(Long id, Integer status) {
        Demand old = demandMapper.selectById(id);
        if (old == null || !old.getBuyerId().equals(UserContext.getUserId())) {
            throw new BizException("只能操作自己的需求");
        }
        old.setStatus(status);
        demandMapper.updateById(old);
    }

    /** 买家：删除需求 */
    public void deleteDemand(Long id) {
        Demand old = demandMapper.selectById(id);
        if (old == null || !old.getBuyerId().equals(UserContext.getUserId())) {
            throw new BizException("只能删除自己的需求");
        }
        demandMapper.deleteById(id);
    }

    /** 给需求补充买家姓名电话 */
    private List<DemandVO> fillDemandVO(List<Demand> list) {
        Map<Long, User> userMap = userMap(list.stream().map(Demand::getBuyerId).toList());
        return list.stream().map(d -> {
            DemandVO vo = new DemandVO();
            BeanUtil.copyProperties(d, vo);
            User u = userMap.get(d.getBuyerId());
            if (u != null) {
                vo.setBuyerName(u.getRealName());
                vo.setBuyerPhone(u.getPhone());
            }
            return vo;
        }).toList();
    }

    private Map<Long, User> userMap(List<Long> ids) {
        List<Long> distinctIds = ids.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (distinctIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(distinctIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
    }
}
