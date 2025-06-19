package com.ruoyi.admin.controller;

import com.ruoyi.domain.other.TtBanner;
import com.ruoyi.admin.service.TtBannerService;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/admin/banner")
public class TtBannerController extends BaseController {

    private final TtBannerService bannerService;

    public TtBannerController(TtBannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping("/list")
    public PageDataInfo<TtBanner> list() {
        startPage();
        List<TtBanner> list = bannerService.list();
        return getPageData(list);
    }

    @GetMapping(value = "/{id}")
    public R<TtBanner> getInfo(@PathVariable("id") Integer id) {
        TtBanner ttBanner = bannerService.getById(id);
//        ttBanner.setPicture("");
        return R.ok(ttBanner);
    }

    @PostMapping
    public AjaxResult add(@RequestBody TtBanner ttBanner) {
        if (StringUtils.isEmpty(ttBanner.getPicture())) ttBanner.setPicture("");
        else ttBanner.setPicture(RuoYiConfig.getDomainName() + ttBanner.getPicture());
        return toAjax(bannerService.save(ttBanner));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody TtBanner ttBanner) {
        String msg = bannerService.updateBannerById(ttBanner);
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids) {
        return toAjax(bannerService.removeByIds(Arrays.asList(ids)));
    }
}
