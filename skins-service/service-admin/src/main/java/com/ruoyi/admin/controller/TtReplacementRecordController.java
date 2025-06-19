package com.ruoyi.admin.controller;

import com.ruoyi.admin.service.TtReplacementRecordService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.domain.other.TtReplacementRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 汰换记录Controller
 * 
 * @author junhai
 * @date 2023-09-10
 */
@RestController
@RequestMapping("/admin/replacementRecord")
public class TtReplacementRecordController extends BaseController
{
    @Autowired
    private TtReplacementRecordService ttReplacementRecordService;

    /**
     * 查询汰换记录列表
     */
    @PreAuthorize("@ss.hasPermi('skinsback:replacementRecord:list')")
    @GetMapping("/list")
    public TableDataInfo list(TtReplacementRecord ttReplacementRecord)
    {
        startPage();
        List<TtReplacementRecord> list = ttReplacementRecordService.selectTtReplacementRecordList(ttReplacementRecord);
        return getDataTable(list);
    }

    /**
     * 导出汰换记录列表
     */
    @PreAuthorize("@ss.hasPermi('skinsback:replacementRecord:export')")
    @Log(title = "汰换记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TtReplacementRecord ttReplacementRecord)
    {
        List<TtReplacementRecord> list = ttReplacementRecordService.selectTtReplacementRecordList(ttReplacementRecord);
        ExcelUtil<TtReplacementRecord> util = new ExcelUtil<TtReplacementRecord>(TtReplacementRecord.class);
        util.exportExcel(response, list, "汰换记录数据");
    }

    /**
     * 获取汰换记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('skinsback:replacementRecord:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(ttReplacementRecordService.selectTtReplacementRecordById(id));
    }

    /**
     * 新增汰换记录
     */
    @PreAuthorize("@ss.hasPermi('skinsback:replacementRecord:add')")
    @Log(title = "汰换记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TtReplacementRecord ttReplacementRecord)
    {
        return toAjax(ttReplacementRecordService.insertTtReplacementRecord(ttReplacementRecord));
    }

    /**
     * 修改汰换记录
     */
    @PreAuthorize("@ss.hasPermi('skinsback:replacementRecord:edit')")
    @Log(title = "汰换记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TtReplacementRecord ttReplacementRecord)
    {
        return toAjax(ttReplacementRecordService.updateTtReplacementRecord(ttReplacementRecord));
    }

    /**
     * 删除汰换记录
     */
    @PreAuthorize("@ss.hasPermi('skinsback:replacementRecord:remove')")
    @Log(title = "汰换记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(ttReplacementRecordService.deleteTtReplacementRecordByIds(ids));
    }
}
