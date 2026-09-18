package com.rongxiaotong.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rongxiaotong.common.BizException;
import com.rongxiaotong.entity.Address;
import com.rongxiaotong.mapper.AddressMapper;
import com.rongxiaotong.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 收货地址服务（买家下单用，个人中心也会用到）
 */
@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressMapper addressMapper;

    /** 我的地址列表（默认地址排最前） */
    public List<Address> list() {
        return addressMapper.selectList(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, UserContext.getUserId())
                        .orderByDesc(Address::getIsDefault));
    }

    /** 保存地址（新增或修改）；设为默认时取消其他默认 */
    public Long save(Address address) {
        if (StrUtil.isBlank(address.getReceiver()) || StrUtil.isBlank(address.getPhone())
                || StrUtil.isBlank(address.getRegion()) || StrUtil.isBlank(address.getDetail())) {
            throw new BizException("请完整填写收货人、电话和地址");
        }
        address.setUserId(UserContext.getUserId());
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearDefault();
        }
        if (address.getId() == null) {
            addressMapper.insert(address);
        } else {
            Address old = addressMapper.selectById(address.getId());
            if (old == null || !old.getUserId().equals(UserContext.getUserId())) {
                throw new BizException("只能修改自己的地址");
            }
            addressMapper.updateById(address);
        }
        return address.getId();
    }

    /** 删除地址 */
    public void remove(Long id) {
        Address old = addressMapper.selectById(id);
        if (old == null || !old.getUserId().equals(UserContext.getUserId())) {
            throw new BizException("只能删除自己的地址");
        }
        addressMapper.deleteById(id);
    }

    /** 把该用户的默认地址全部取消 */
    private void clearDefault() {
        List<Address> defaults = addressMapper.selectList(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, UserContext.getUserId())
                        .eq(Address::getIsDefault, 1));
        for (Address a : defaults) {
            a.setIsDefault(0);
            addressMapper.updateById(a);
        }
    }
}
