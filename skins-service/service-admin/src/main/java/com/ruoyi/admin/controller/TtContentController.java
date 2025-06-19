package com.ruoyi.admin.controller;

import com.ruoyi.domain.other.TtContent;
import com.ruoyi.admin.service.TtContentService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/admin/content")
public class TtContentController extends BaseController {

    private final TtContentService contentService;

    public TtContentController(TtContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/list")
    public PageDataInfo<TtContent> list(TtContent ttContent) {
        startPage();
        List<TtContent> list = contentService.queryList(ttContent);
        return getPageData(list);
    }

    @GetMapping(value = "/{id}")
    public R<TtContent> getInfo(@PathVariable("id") Long id) {
        return R.ok(contentService.getById(id));
    }

    @PostMapping
    public AjaxResult add(@RequestBody TtContent ttContent) {
        ttContent.setCreateTime(DateUtils.getNowDate());
        return toAjax(contentService.save(ttContent));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody TtContent ttContent) {
        ttContent.setUpdateTime(DateUtils.getNowDate());
        return toAjax(contentService.updateById(ttContent));
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids) {
        return toAjax(contentService.removeByIds(Arrays.asList(ids)));
    }
}
