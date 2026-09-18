package com.rongxiaotong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rongxiaotong.common.BizException;
import com.rongxiaotong.common.Roles;
import com.rongxiaotong.dto.CartItemVO;
import com.rongxiaotong.entity.Cart;
import com.rongxiaotong.entity.Product;
import com.rongxiaotong.entity.User;
import com.rongxiaotong.mapper.CartMapper;
import com.rongxiaotong.mapper.ProductMapper;
import com.rongxiaotong.mapper.UserMapper;
import com.rongxiaotong.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 购物车服务
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    /** 我的购物车（只展示上架商品，含商品信息和农户名） */
    public List<CartItemVO> list() {
        List<Cart> carts = cartMapper.selectList(
                new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, UserContext.getUserId()));
        if (carts.isEmpty()) {
            return List.of();
        }
        List<Long> productIds = carts.stream().map(Cart::getProductId).distinct().toList();
        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, User> userMap = userMapper.selectBatchIds(
                        productMap.values().stream().map(Product::getFarmerId).distinct().toList())
                .stream().collect(Collectors.toMap(User::getId, u -> u));
        return carts.stream()
                .filter(c -> productMap.containsKey(c.getProductId()))
                .filter(c -> productMap.get(c.getProductId()).getStatus() == 1)   // 过滤已下架商品
                .map(c -> {
                    Product p = productMap.get(c.getProductId());
                    CartItemVO vo = new CartItemVO();
                    vo.setId(c.getId());
                    vo.setProductId(p.getId());
                    vo.setQuantity(c.getQuantity());
                    vo.setProductName(p.getName());
                    vo.setCover(p.getCover());
                    vo.setPrice(p.getPrice());
                    vo.setUnit(p.getUnit());
                    vo.setStock(p.getStock());
                    vo.setSubtotal(Math.round(p.getPrice() * c.getQuantity() * 100) / 100.0);
                    User u = userMap.get(p.getFarmerId());
                    vo.setFarmerName(u == null ? "" : u.getRealName());
                    return vo;
                }).toList();
    }

    /** 加入购物车（已在车里则数量累加） */
    public void add(Long productId, Integer quantity) {
        if (!UserContext.isRole(Roles.BUYER)) {
            throw new BizException("只有买家可以使用购物车");
        }
        Product p = productMapper.selectById(productId);
        if (p == null || p.getStatus() != 1) {
            throw new BizException("商品不存在或已下架");
        }
        Cart exist = cartMapper.selectOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, UserContext.getUserId())
                .eq(Cart::getProductId, productId));
        int newQty = (exist == null ? 0 : exist.getQuantity()) + (quantity == null ? 1 : quantity);
        if (newQty > p.getStock()) {
            throw new BizException("库存不足，当前仅剩 " + p.getStock() + " " + p.getUnit());
        }
        if (exist == null) {
            Cart cart = new Cart();
            cart.setUserId(UserContext.getUserId());
            cart.setProductId(productId);
            cart.setQuantity(newQty);
            cartMapper.insert(cart);
        } else {
            exist.setQuantity(newQty);
            cartMapper.updateById(exist);
        }
    }

    /** 修改购物车数量 */
    public void updateQuantity(Long id, Integer quantity) {
        Cart cart = cartMapper.selectById(id);
        if (cart == null || !cart.getUserId().equals(UserContext.getUserId())) {
            throw new BizException("购物车记录不存在");
        }
        Product p = productMapper.selectById(cart.getProductId());
        if (quantity == null || quantity < 1) {
            throw new BizException("数量不能小于 1");
        }
        if (p != null && quantity > p.getStock()) {
            throw new BizException("库存不足，当前仅剩 " + p.getStock() + " " + p.getUnit());
        }
        cart.setQuantity(quantity);
        cartMapper.updateById(cart);
    }

    /** 删除购物车条目 */
    public void remove(Long id) {
        Cart cart = cartMapper.selectById(id);
        if (cart == null || !cart.getUserId().equals(UserContext.getUserId())) {
            throw new BizException("购物车记录不存在");
        }
        cartMapper.deleteById(id);
    }
}
