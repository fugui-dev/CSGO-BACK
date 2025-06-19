package com.ruoyi.admin.controller;

import com.ruoyi.domain.other.TtVipLevel;
import com.ruoyi.admin.service.TtVipLevelService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/vipLevel")
public class TtVipLevelController extends BaseController {

    private final TtVipLevelService vipLevelService;

    public TtVipLevelController(TtVipLevelService vipLevelService) {
        this.vipLevelService = vipLevelService;
    }

    @GetMapping("/list")
    public PageDataInfo<TtVipLevel> list() {
        startPage();
        List<TtVipLevel> list = vipLevelService.list();
        return getPageData(list);
    }

    @GetMapping(value = "/{id}")
    public R<TtVipLevel> getInfo(@PathVariable("id") Integer id) {
        TtVipLevel ttVipLevel = vipLevelService.getById(id);
//        ttVipLevel.setIcon("");
        return R.ok(ttVipLevel);
    }

    @PostMapping("generateVipLevel/{num}")
    public AjaxResult generateVipLevel(@PathVariable("num") Integer num) {
        String msg = vipLevelService.generateVipLevel(num);
        return StringUtils.isEmpty(msg) ? AjaxResult.success("生成成功，请修改参数！") : AjaxResult.error(msg);
    }

    @PutMapping
    public AjaxResult edit(@RequestBody TtVipLevel ttVipLevel) {
        ttVipLevel.setUpdateTime(DateUtils.getNowDate());
        String msg = vipLevelService.updateVipLevelById(ttVipLevel);
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }

    @PostMapping
    public AjaxResult truncateVipLevel() {
        vipLevelService.truncateVipLevel();
        return AjaxResult.success("重置成功，请重新设置！");
    }
}
