package com.ruoyi.admin.controller;

import com.ruoyi.domain.entity.TtPromotionLevel;
import com.ruoyi.admin.service.TtPromotionLevelService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/promotionLevel")
public class TtPromotionLevelController extends BaseController {

    private final TtPromotionLevelService promotionLevelService;

    public TtPromotionLevelController(TtPromotionLevelService promotionLevelService) {
        this.promotionLevelService = promotionLevelService;
    }

    @GetMapping("/list")
    public PageDataInfo<TtPromotionLevel> list() {
        startPage();
        List<TtPromotionLevel> list = promotionLevelService.list();
        return getPageData(list);
    }

    @GetMapping(value = "/{id}")
    public R<TtPromotionLevel> getInfo(@PathVariable("id") Integer id) {
        TtPromotionLevel ttPromotionLevel = promotionLevelService.getById(id);
//        ttPromotionLevel.setIcon("");
        return R.ok(ttPromotionLevel);
    }

    @PostMapping("generatePromotionLevel/{num}")
    public AjaxResult generatePromotionLevel(@PathVariable("num") Integer num) {
        String msg = promotionLevelService.generateVipLevel(num);
        return StringUtils.isEmpty(msg) ? AjaxResult.success("生成成功，请修改参数！") : AjaxResult.error(msg);
    }

    @PutMapping
    public AjaxResult edit(@RequestBody TtPromotionLevel ttPromotionLevel) {
        ttPromotionLevel.setUpdateTime(DateUtils.getNowDate());
        String msg = promotionLevelService.updatePromotionLevelById(ttPromotionLevel);
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }

    @PostMapping
    public AjaxResult truncateVipLevel() {
        promotionLevelService.truncatePromotionLevel();
        return AjaxResult.success("重置成功，请重新设置！");
    }
}
