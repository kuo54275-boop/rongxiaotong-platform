package com.rongxiaotong.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rongxiaotong.common.BizException;
import com.rongxiaotong.common.Roles;
import com.rongxiaotong.dto.OrderCreateDTO;
import com.rongxiaotong.dto.OrderItemVO;
import com.rongxiaotong.dto.OrderVO;
import com.rongxiaotong.entity.*;
import com.rongxiaotong.mapper.*;
import com.rongxiaotong.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单服务：买家下单付款，农户发货
 * 状态流转：UNPAID 待付款 →(付款) SHIPPED 待发货 →(发货) COMPLETED 已完成
 *          UNPAID →(取消) CANCELLED 已取消（恢复库存）
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    /**
     * 下单：把购物车中的商品按农户分组生成订单（一个农户一个订单）
     * 扣减库存、清空购物车，全程事务保证一致性
     */
    @Transactional
    public List<Long> create(OrderCreateDTO dto) {
        if (!UserContext.isRole(Roles.BUYER)) {
            throw new BizException("只有买家可以下单");
        }
        if (dto.getAddressId() == null) {
            throw new BizException("请选择收货地址");
        }
        Address address = addressMapper.selectById(dto.getAddressId());
        if (address == null || !address.getUserId().equals(UserContext.getUserId())) {
            throw new BizException("收货地址不存在");
        }
        // 1. 读取购物车
        List<Cart> carts = cartMapper.selectList(
                new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, UserContext.getUserId()));
        if (carts.isEmpty()) {
            throw new BizException("购物车是空的，先去挑选商品吧");
        }
        // 2. 校验商品与库存
        List<Long> productIds = carts.stream().map(Cart::getProductId).distinct().toList();
        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        for (Cart c : carts) {
            Product p = productMap.get(c.getProductId());
            if (p == null || p.getStatus() != 1) {
                throw new BizException("有商品已下架，请先移除后再下单");
            }
            if (c.getQuantity() > p.getStock()) {
                throw new BizException("《" + p.getName() + "》库存不足，仅剩 " + p.getStock() + " " + p.getUnit());
            }
        }
        // 3. 按农户分组生成订单
        Map<Long, List<Cart>> grouped = carts.stream().collect(Collectors.groupingBy(
                c -> productMap.get(c.getProductId()).getFarmerId()));
        List<Long> orderIds = new ArrayList<>();
        for (Map.Entry<Long, List<Cart>> entry : grouped.entrySet()) {
            Long farmerId = entry.getKey();
            List<Cart> items = entry.getValue();
            double total = 0;
            Order order = new Order();
            order.setOrderNo("RXT" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(3));
            order.setBuyerId(UserContext.getUserId());
            order.setFarmerId(farmerId);
            order.setAddressId(dto.getAddressId());
            order.setStatus("UNPAID");
            order.setCreateTime(DateUtil.now());
            for (Cart c : items) {
                Product p = productMap.get(c.getProductId());
                total += p.getPrice() * c.getQuantity();
            }
            order.setTotalAmount(Math.round(total * 100) / 100.0);
            orderMapper.insert(order);
            // 4. 订单明细 + 扣库存
            for (Cart c : items) {
                Product p = productMap.get(c.getProductId());
                OrderItem oi = new OrderItem();
                oi.setOrderId(order.getId());
                oi.setProductId(p.getId());
                oi.setPrice(p.getPrice());
                oi.setQuantity(c.getQuantity());
                orderItemMapper.insert(oi);
                p.setStock(p.getStock() - c.getQuantity());
                productMapper.updateById(p);
            }
            orderIds.add(order.getId());
            // 5. 通知农户：有新订单
            User buyer = userMapper.selectById(UserContext.getUserId());
            notificationService.send(farmerId, "ORDER_NEW", "🛒 新订单提醒",
                    "买家" + (buyer == null ? "" : buyer.getRealName())
                            + "提交了新订单 " + order.getOrderNo() + "，金额 ¥" + order.getTotalAmount() + "，请及时关注",
                    order.getId());
        }
        // 5. 清空购物车
        cartMapper.delete(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, UserContext.getUserId()));
        return orderIds;
    }

    /** 买家：我的订单（含明细、地址、农户名） */
    public List<OrderVO> myOrders() {
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getBuyerId, UserContext.getUserId())
                        .orderByDesc(Order::getCreateTime));
        return fillOrderVO(orders);
    }

    /** 农户：收到的订单（含明细、地址、买家名） */
    public List<OrderVO> sellerOrders() {
        if (!UserContext.isRole(Roles.FARMER)) {
            throw new BizException("只有农户可以查看供货订单");
        }
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getFarmerId, UserContext.getUserId())
                        .orderByDesc(Order::getCreateTime));
        return fillOrderVO(orders);
    }

    /** 买家：付款（模拟，待付款 → 待发货） */
    public void pay(Long id) {
        Order order = mustOwnOrder(id, true);
        if (!"UNPAID".equals(order.getStatus())) {
            throw new BizException("该订单当前状态不能付款");
        }
        order.setStatus("SHIPPED");
        orderMapper.updateById(order);
    }

    /** 农户：发货（待发货 → 已完成），并通知买家 */
    public void ship(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null || !order.getFarmerId().equals(UserContext.getUserId())) {
            throw new BizException("订单不存在");
        }
        if (!"SHIPPED".equals(order.getStatus())) {
            throw new BizException("该订单当前状态不能发货");
        }
        order.setStatus("COMPLETED");
        orderMapper.updateById(order);
        notificationService.send(order.getBuyerId(), "ORDER_SHIPPED", "🚚 订单已发货",
                "您的订单 " + order.getOrderNo() + " 已发货，请注意查收", order.getId());
    }

    /** 买家：取消订单（恢复库存） */
    @Transactional
    public void cancel(Long id) {
        Order order = mustOwnOrder(id, true);
        if (!"UNPAID".equals(order.getStatus())) {
            throw new BizException("只有待付款的订单可以取消");
        }
        order.setStatus("CANCELLED");
        orderMapper.updateById(order);
        // 恢复库存
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        for (OrderItem oi : items) {
            Product p = productMapper.selectById(oi.getProductId());
            if (p != null) {
                p.setStock(p.getStock() + oi.getQuantity());
                productMapper.updateById(p);
            }
        }
    }

    /** 校验订单归属 */
    private Order mustOwnOrder(Long id, boolean isBuyer) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        boolean owned = isBuyer
                ? order.getBuyerId().equals(UserContext.getUserId())
                : order.getFarmerId().equals(UserContext.getUserId());
        if (!owned) {
            throw new BizException("订单不存在");
        }
        return order;
    }

    /** 组装订单展示对象 */
    private List<OrderVO> fillOrderVO(List<Order> orders) {
        if (orders.isEmpty()) {
            return List.of();
        }
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        // 明细
        Map<Long, List<OrderItem>> itemMap = orderItemMapper.selectList(
                        new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds))
                .stream().collect(Collectors.groupingBy(OrderItem::getOrderId));
        // 商品信息
        List<Long> productIds = itemMap.values().stream().flatMap(List::stream)
                .map(OrderItem::getProductId).distinct().toList();
        Map<Long, Product> productMap = productIds.isEmpty() ? Map.of()
                : productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        // 用户信息
        Set<Long> userIds = new HashSet<>();
        orders.forEach(o -> {
            userIds.add(o.getBuyerId());
            userIds.add(o.getFarmerId());
        });
        Map<Long, User> userMap = userMapper.selectBatchIds(new ArrayList<>(userIds)).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        // 地址信息
        Map<Long, Address> addressMap = addressMapper.selectBatchIds(
                        orders.stream().map(Order::getAddressId).distinct().toList())
                .stream().collect(Collectors.toMap(Address::getId, a -> a));

        return orders.stream().map(o -> {
            OrderVO vo = new OrderVO();
            vo.setId(o.getId());
            vo.setOrderNo(o.getOrderNo());
            vo.setBuyerId(o.getBuyerId());
            vo.setFarmerId(o.getFarmerId());
            vo.setTotalAmount(o.getTotalAmount());
            vo.setStatus(o.getStatus());
            vo.setCreateTime(o.getCreateTime());
            User b = userMap.get(o.getBuyerId());
            User f = userMap.get(o.getFarmerId());
            vo.setBuyerName(b == null ? "" : b.getRealName());
            vo.setFarmerName(f == null ? "" : f.getRealName());
            Address a = addressMap.get(o.getAddressId());
            vo.setAddressText(a == null ? "" : a.getRegion() + " " + a.getDetail() + "（" + a.getReceiver() + " " + a.getPhone() + "）");
            vo.setItems(itemMap.getOrDefault(o.getId(), List.of()).stream().map(oi -> {
                OrderItemVO iv = new OrderItemVO();
                iv.setId(oi.getId());
                iv.setProductId(oi.getProductId());
                iv.setPrice(oi.getPrice());
                iv.setQuantity(oi.getQuantity());
                iv.setSubtotal(Math.round(oi.getPrice() * oi.getQuantity() * 100) / 100.0);
                Product p = productMap.get(oi.getProductId());
                if (p != null) {
                    iv.setProductName(p.getName());
                    iv.setCover(p.getCover());
                    iv.setUnit(p.getUnit());
                }
                return iv;
            }).toList());
            return vo;
        }).toList();
    }
}
