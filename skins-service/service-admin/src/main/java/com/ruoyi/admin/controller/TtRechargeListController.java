package com.ruoyi.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.domain.entity.TtRechargeProd;
import com.ruoyi.admin.service.TtRechargeProdService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/admin/rechargeList")
public class TtRechargeListController extends BaseController {

    private final TtRechargeProdService rechargeListService;

    public TtRechargeListController(TtRechargeProdService rechargeListService) {
        this.rechargeListService = rechargeListService;
    }

    @GetMapping("/list")
    public PageDataInfo<TtRechargeProd> list(@RequestParam(required = false) String status) {
        startPage();
        LambdaQueryWrapper<TtRechargeProd> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotEmpty(status)) wrapper.eq(TtRechargeProd::getStatus, status);
        List<TtRechargeProd> list = rechargeListService.list(wrapper);
        return getPageData(list);
    }

    @PreAuthorize("@ss.hasPermi('admin:rechargeList:query')")
    @GetMapping(value = "/{id}")
    public R<TtRechargeProd> getInfo(@PathVariable("id") Integer id) {
        TtRechargeProd rechargeList = rechargeListService.getById(id);
        return R.ok(rechargeList);
    }

    @PostMapping
    public AjaxResult add(@RequestBody TtRechargeProd ttRechargeProd) {
        return toAjax(rechargeListService.save(ttRechargeProd));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody TtRechargeProd ttRechargeProd) {
        return toAjax(rechargeListService.updateById(ttRechargeProd));
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids) {
        return toAjax(rechargeListService.removeByIds(Arrays.asList(ids)));
    }

}
