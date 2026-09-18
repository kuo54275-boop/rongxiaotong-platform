package com.rongxiaotong.controller;

import com.rongxiaotong.common.Result;
import com.rongxiaotong.dto.DemandVO;
import com.rongxiaotong.entity.Demand;
import com.rongxiaotong.service.DemandService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 求购需求接口（买家管理，需登录）
 */
@RestController
@RequestMapping("/demand")
@RequiredArgsConstructor
public class DemandController {

    private final DemandService demandService;

    /** 我的需求 */
    @GetMapping("/my")
    public Result<List<DemandVO>> myDemands() {
        return Result.ok(demandService.myDemands());
    }

    /** 发布 / 修改需求 */
    @PostMapping("/save")
    public Result<Long> save(@RequestBody Demand demand) {
        return Result.ok(demand.getId() == null ? "发布成功" : "修改成功", demandService.saveDemand(demand));
    }

    /** 结束 / 重新开启需求 */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        demandService.updateStatus(id, status);
        return Result.ok("操作成功", null);
    }

    /** 删除需求 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        demandService.deleteDemand(id);
        return Result.ok("删除成功", null);
    }
}
