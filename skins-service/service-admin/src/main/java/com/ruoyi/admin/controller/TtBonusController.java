package com.ruoyi.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.domain.other.TtBonus;
import com.ruoyi.admin.service.TtBonusService;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/admin/bonus")
public class TtBonusController extends BaseController {

    private final TtBonusService bonusService;

    public TtBonusController(TtBonusService bonusService) {
        this.bonusService = bonusService;
    }

    @GetMapping("/list")
    public PageDataInfo<TtBonus> list(String type){
        LambdaQueryWrapper<TtBonus> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotEmpty(type)) wrapper.eq(TtBonus::getType, type);
        startPage();
        List<TtBonus> list = bonusService.list(wrapper);
        return getPageData(list);
    }

    @GetMapping(value = "/{id}")
    public R<TtBonus> getInfo(@PathVariable("id") Integer id) {
        TtBonus bonus = bonusService.getById(id);
//        bonus.setCoverPicture("");
        return R.ok(bonus);
    }

    @PostMapping
    public AjaxResult add(@RequestBody TtBonus ttBonus) {
        if (StringUtils.isEmpty(ttBonus.getCoverPicture())) ttBonus.setCoverPicture("");
        else ttBonus.setCoverPicture(RuoYiConfig.getDomainName() + ttBonus.getCoverPicture());
        ttBonus.setCreateBy(getUsername());
        ttBonus.setCreateTime(DateUtils.getNowDate());
        return toAjax(bonusService.save(ttBonus));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody TtBonus ttBonus) {
        ttBonus.setUpdateBy(getUsername());
        ttBonus.setUpdateTime(DateUtils.getNowDate());
        String msg = bonusService.updateBonusById(ttBonus);
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids) {
        return toAjax(bonusService.removeByIds(Arrays.asList(ids)));
    }

}
