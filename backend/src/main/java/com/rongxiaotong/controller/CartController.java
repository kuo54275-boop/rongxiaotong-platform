package com.rongxiaotong.controller;

import com.rongxiaotong.common.Result;
import com.rongxiaotong.dto.CartItemVO;
import com.rongxiaotong.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车接口
 */
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /** 我的购物车 */
    @GetMapping("/list")
    public Result<List<CartItemVO>> list() {
        return Result.ok(cartService.list());
    }

    /** 加入购物车 */
    @PostMapping("/add")
    public Result<Void> add(@RequestParam Long productId, @RequestParam(defaultValue = "1") Integer quantity) {
        cartService.add(productId, quantity);
        return Result.ok("已加入购物车", null);
    }

    /** 修改数量 */
    @PutMapping("/{id}/quantity")
    public Result<Void> updateQuantity(@PathVariable Long id, @RequestParam Integer quantity) {
        cartService.updateQuantity(id, quantity);
        return Result.ok("已更新", null);
    }

    /** 删除条目 */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        cartService.remove(id);
        return Result.ok("已移除", null);
    }
}
