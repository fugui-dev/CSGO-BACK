package com.ruoyi.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.domain.entity.roll.TtRollJackpot;
import com.ruoyi.admin.service.TtRollJackpotService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Api(tags = "管理端 roll奖池")
@RestController
@RequestMapping("/admin/rollJackpot")
public class TtRollJackpotController extends BaseController {

    private final TtRollJackpotService rollJackpotService;

    public TtRollJackpotController(TtRollJackpotService rollJackpotService) {
        this.rollJackpotService = rollJackpotService;
    }

    @ApiOperation("奖池列表")
    @GetMapping("/list")
    public PageDataInfo<TtRollJackpot> list(@RequestParam(value = "jackpotName", required = false) String jackpotName) {
        startPage();
        LambdaQueryWrapper<TtRollJackpot> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotEmpty(jackpotName)) {
            wrapper.like(TtRollJackpot::getJackpotName, jackpotName);
        }
        List<TtRollJackpot> list = rollJackpotService.list(wrapper);
        return getPageData(list);
    }

    @GetMapping(value = "/{jackpotId}")
    public R<TtRollJackpot> getInfo(@PathVariable("jackpotId") Long jackpotId) {
        TtRollJackpot rollJackpot = rollJackpotService.getById(jackpotId);
        return R.ok(rollJackpot);
    }

    @ApiOperation("添加奖池")
    @PostMapping
    public AjaxResult add(@RequestBody TtRollJackpot ttRollJackpot) {
        ttRollJackpot.setCreateBy(getUsername());
        ttRollJackpot.setCreateTime(DateUtils.getNowDate());
        ttRollJackpot.setTotalPrice(BigDecimal.ZERO);
        return toAjax(rollJackpotService.save(ttRollJackpot));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody TtRollJackpot ttRollJackpot) {
        ttRollJackpot.setUpdateBy(getUsername());
        ttRollJackpot.setUpdateTime(DateUtils.getNowDate());
        return toAjax(rollJackpotService.updateById(ttRollJackpot));
    }

    @DeleteMapping("/remove/{jackpotId}")
    public AjaxResult remove(@PathVariable Long jackpotId) {
        String msg = rollJackpotService.removeRollJackpotById(jackpotId);
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }
}
