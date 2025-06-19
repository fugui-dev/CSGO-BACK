package com.ruoyi.admin.controller;

import com.ruoyi.domain.other.TtContentType;
import com.ruoyi.admin.service.TtContentTypeService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/admin/contentType")
public class TtContentTypeController extends BaseController {

    private final TtContentTypeService contentTypeService;

    public TtContentTypeController(TtContentTypeService contentTypeService) {
        this.contentTypeService = contentTypeService;
    }

    @GetMapping("/list")
    public PageDataInfo<TtContentType> list(TtContentType ttContentType) {
        startPage();
        List<TtContentType> list = contentTypeService.queryList(ttContentType);
        return getPageData(list);
    }

    @GetMapping(value = "/{id}")
    public R<TtContentType> getInfo(@PathVariable("id") Long id) {
        return R.ok(contentTypeService.getById(id));
    }

    @PostMapping
    public AjaxResult add(@RequestBody TtContentType ttContentType) {
        ttContentType.setCreateTime(DateUtils.getNowDate());
        return toAjax(contentTypeService.save(ttContentType));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody TtContentType ttContentType) {
        ttContentType.setUpdateTime(DateUtils.getNowDate());
        return toAjax(contentTypeService.updateById(ttContentType));
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids) {
        return toAjax(contentTypeService.removeByIds(Arrays.asList(ids)));
    }
}
