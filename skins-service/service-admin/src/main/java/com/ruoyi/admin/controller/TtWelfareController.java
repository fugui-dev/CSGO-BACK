package com.ruoyi.admin.controller;

import com.ruoyi.admin.service.TtWelfareService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.domain.other.TtWelfare;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 福利列表Controller
 *
 * @author ruoyi
 * @date 2024-05-11
 */
@RestController
@RequestMapping("/admin/welfare")
public class TtWelfareController extends BaseController
{
    @Autowired
    private TtWelfareService ttWelfareService;

    /**
     * 查询福利列表
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    public TableDataInfo list(TtWelfare ttWelfare)
    {
        startPage();
        List<TtWelfare> list = ttWelfareService.selectTtWelfareList(ttWelfare);
        return getDataTable(list);
    }

    /**
     * 导出福利列表
     */
    @PreAuthorize("isAuthenticated()")
    @Log(title = "福利列表", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TtWelfare ttWelfare)
    {
        List<TtWelfare> list = ttWelfareService.selectTtWelfareList(ttWelfare);
        ExcelUtil<TtWelfare> util = new ExcelUtil<TtWelfare>(TtWelfare.class);
        util.exportExcel(response, list, "福利列表数据");
    }

    /**
     * 获取福利列表详细信息
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/{welfareId}")
    public AjaxResult getInfo(@PathVariable("welfareId") Integer welfareId)
    {
        return success(ttWelfareService.selectTtWelfareByWelfareId(welfareId));
    }

    /**
     * 新增福利列表
     */
    @PreAuthorize("isAuthenticated()")
    @Log(title = "福利列表", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TtWelfare ttWelfare)
    {
        return toAjax(ttWelfareService.insertTtWelfare(ttWelfare));
    }

    /**
     * 修改福利列表
     */
    @PreAuthorize("isAuthenticated()")
    @Log(title = "福利列表", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TtWelfare ttWelfare)
    {
        return toAjax(ttWelfareService.updateTtWelfare(ttWelfare));
    }

    /**
     * 删除福利列表
     */
    @PreAuthorize("isAuthenticated()")
    @Log(title = "福利列表", businessType = BusinessType.DELETE)
    @DeleteMapping("/{welfareIds}")
    public AjaxResult remove(@PathVariable Integer[] welfareIds)
    {
        return toAjax(ttWelfareService.deleteTtWelfareByWelfareIds(welfareIds));
    }
}

