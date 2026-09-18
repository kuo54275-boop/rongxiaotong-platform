package com.rongxiaotong.controller;

import com.rongxiaotong.common.Result;
import com.rongxiaotong.entity.Address;
import com.rongxiaotong.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址接口
 */
@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /** 我的地址列表 */
    @GetMapping("/list")
    public Result<List<Address>> list() {
        return Result.ok(addressService.list());
    }

    /** 保存地址（新增或修改） */
    @PostMapping("/save")
    public Result<Long> save(@RequestBody Address address) {
        return Result.ok("保存成功", addressService.save(address));
    }

    /** 删除地址 */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        addressService.remove(id);
        return Result.ok("删除成功", null);
    }
}
