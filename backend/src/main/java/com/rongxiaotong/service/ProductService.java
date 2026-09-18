package com.rongxiaotong.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rongxiaotong.common.BizException;
import com.rongxiaotong.common.PageResult;
import com.rongxiaotong.common.Roles;
import com.rongxiaotong.dto.ProductVO;
import com.rongxiaotong.entity.FarmerProfile;
import com.rongxiaotong.entity.Product;
import com.rongxiaotong.entity.User;
import com.rongxiaotong.mapper.FarmerProfileMapper;
import com.rongxiaotong.mapper.ProductMapper;
import com.rongxiaotong.mapper.UserMapper;
import com.rongxiaotong.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 农产品服务：商品浏览（买家）、商品管理（农户）
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final FarmerProfileMapper farmerProfileMapper;

    /** 商品列表（分页 + 分类 + 关键词搜索，只展示上架商品） */
    public PageResult<ProductVO> pageProducts(long page, long size, String category, String keyword) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .eq(StrUtil.isNotBlank(category), Product::getCategory, category)
                .like(StrUtil.isNotBlank(keyword), Product::getName, keyword)
                .orderByDesc(Product::getCreateTime);
        Page<Product> p = productMapper.selectPage(new Page<>(page, size), qw);
        List<ProductVO> list = fillProductVO(p.getRecords(), false);
        return PageResult.of(list, p.getTotal(), p.getCurrent(), p.getSize());
    }

    /** 商品详情（含农户姓名和产地） */
    public ProductVO productDetail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException("商品不存在");
        }
        return fillProductVO(List.of(product), true).get(0);
    }

    /** 农户：我的商品 */
    public List<ProductVO> myProducts() {
        List<Product> list = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getFarmerId, UserContext.getUserId())
                        .orderByDesc(Product::getCreateTime));
        return fillProductVO(list, false);
    }

    /** 农户：发布 / 修改商品 */
    public Long saveProduct(Product product) {
        if (!UserContext.isRole(Roles.FARMER)) {
            throw new BizException("只有农户可以发布商品");
        }
        if (StrUtil.isBlank(product.getName()) || product.getPrice() == null || product.getStock() == null) {
            throw new BizException("请填写商品名称、价格和库存");
        }
        if (product.getId() == null) {
            product.setFarmerId(UserContext.getUserId());
            product.setStatus(1);
            product.setCreateTime(DateUtil.now());
            productMapper.insert(product);
        } else {
            Product old = productMapper.selectById(product.getId());
            if (old == null || !old.getFarmerId().equals(UserContext.getUserId())) {
                throw new BizException("只能修改自己的商品");
            }
            product.setFarmerId(old.getFarmerId());
            product.setStatus(old.getStatus());
            product.setCreateTime(old.getCreateTime());
            productMapper.updateById(product);
        }
        return product.getId();
    }

    /** 农户：上架 / 下架 */
    public void updateStatus(Long id, Integer status) {
        Product old = productMapper.selectById(id);
        if (old == null || !old.getFarmerId().equals(UserContext.getUserId())) {
            throw new BizException("只能操作自己的商品");
        }
        old.setStatus(status);
        productMapper.updateById(old);
    }

    /** 农户：删除商品 */
    public void deleteProduct(Long id) {
        Product old = productMapper.selectById(id);
        if (old == null || !old.getFarmerId().equals(UserContext.getUserId())) {
            throw new BizException("只能删除自己的商品");
        }
        productMapper.deleteById(id);
    }

    /** 给商品补充农户姓名（withAddress=true 时补充产地） */
    private List<ProductVO> fillProductVO(List<Product> list, boolean withAddress) {
        Map<Long, User> userMap = userMap(list.stream().map(Product::getFarmerId).toList());
        Map<Long, String> addressMap = withAddress
                ? farmerProfileMapper.selectList(new LambdaQueryWrapper<FarmerProfile>()
                        .in(FarmerProfile::getUserId, userMap.keySet()))
                .stream().collect(Collectors.toMap(FarmerProfile::getUserId, fp -> StrUtil.nullToEmpty(fp.getAddress())))
                : Map.of();
        return list.stream().map(p -> {
            ProductVO vo = new ProductVO();
            BeanUtil.copyProperties(p, vo);
            User u = userMap.get(p.getFarmerId());
            if (u != null) {
                vo.setFarmerName(u.getRealName());
            }
            if (withAddress) {
                vo.setFarmerAddress(addressMap.getOrDefault(p.getFarmerId(), ""));
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
