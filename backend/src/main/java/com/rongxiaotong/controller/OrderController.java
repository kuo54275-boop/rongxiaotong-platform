package com.rongxiaotong.controller;

import com.rongxiaotong.common.Result;
import com.rongxiaotong.dto.OrderCreateDTO;
import com.rongxiaotong.dto.OrderVO;
import com.rongxiaotong.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单接口
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** 下单（从购物车结算） */
    @PostMapping("/create")
    public Result<List<Long>> create(@RequestBody OrderCreateDTO dto) {
        return Result.ok("下单成功", orderService.create(dto));
    }

    /** 买家：我的订单 */
    @GetMapping("/my")
    public Result<List<OrderVO>> myOrders() {
        return Result.ok(orderService.myOrders());
    }

    /** 农户：供货订单 */
    @GetMapping("/seller")
    public Result<List<OrderVO>> sellerOrders() {
        return Result.ok(orderService.sellerOrders());
    }

    /** 买家：付款 */
    @PutMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id) {
        orderService.pay(id);
        return Result.ok("付款成功，等待农户发货", null);
    }

    /** 农户：发货 */
    @PutMapping("/{id}/ship")
    public Result<Void> ship(@PathVariable Long id) {
        orderService.ship(id);
        return Result.ok("发货成功", null);
    }

    /** 买家：取消订单 */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return Result.ok("订单已取消", null);
    }
}
