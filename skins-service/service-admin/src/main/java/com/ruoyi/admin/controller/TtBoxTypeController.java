package com.ruoyi.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.common.utils.PageUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.domain.other.TtBoxType;
import com.ruoyi.admin.service.TtBoxTypeService;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/admin/boxType")
@Slf4j
public class TtBoxTypeController extends BaseController {

    private final TtBoxTypeService boxTypeService;

    public TtBoxTypeController(TtBoxTypeService boxTypeService) {
        this.boxTypeService = boxTypeService;
    }

    @GetMapping("/list")
    public PageDataInfo<TtBoxType> list(@RequestParam(required = false) String isFightType) {
        if (ServletUtils.getParameter(TableSupport.PAGE_NUM) != null
            && ServletUtils.getParameter(TableSupport.PAGE_SIZE) != null){
            startPage();
        }
        LambdaQueryWrapper<TtBoxType> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotEmpty(isFightType)) wrapper.eq(TtBoxType::getIsFightType, isFightType);
        List<TtBoxType> list = boxTypeService.list(wrapper);
        return getPageData(list);
    }

    @GetMapping(value = "/{id}")
    public R<TtBoxType> getInfo(@PathVariable("id") Integer id) {
        TtBoxType boxType = boxTypeService.getById(id);
//        boxType.setIcon("");
        return R.ok(boxType);
    }

    @PostMapping
    public AjaxResult add(@RequestBody TtBoxType ttBoxType) {
        if (StringUtils.isEmpty(ttBoxType.getIcon())) ttBoxType.setIcon("");
        else ttBoxType.setIcon(RuoYiConfig.getDomainName() + ttBoxType.getIcon());
        ttBoxType.setCreateTime(DateUtils.getNowDate());
        return toAjax(boxTypeService.save(ttBoxType));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody TtBoxType ttBoxType) {
        ttBoxType.setUpdateTime(DateUtils.getNowDate());
        String msg = boxTypeService.updateBoxTypeById(ttBoxType);
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids) {
        return toAjax(boxTypeService.removeByIds(Arrays.asList(ids)));
    }

}
