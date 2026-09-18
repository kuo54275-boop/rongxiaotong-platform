package com.rongxiaotong.controller;

import com.rongxiaotong.common.Result;
import com.rongxiaotong.dto.ProductVO;
import com.rongxiaotong.entity.Product;
import com.rongxiaotong.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品接口（农户管理，需登录）
 */
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /** 我的商品 */
    @GetMapping("/my")
    public Result<List<ProductVO>> myProducts() {
        return Result.ok(productService.myProducts());
    }

    /** 发布 / 修改商品 */
    @PostMapping("/save")
    public Result<Long> save(@RequestBody Product product) {
        return Result.ok(product.getId() == null ? "发布成功" : "修改成功", productService.saveProduct(product));
    }

    /** 上架 / 下架 */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.updateStatus(id, status);
        return Result.ok("操作成功", null);
    }

    /** 删除商品 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.ok("删除成功", null);
    }
}
