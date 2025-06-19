package com.ruoyi.admin.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.domain.other.TtFirstRecharge;
import com.ruoyi.admin.service.TtFirstRechargeService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 首充赠送Controller
 *
 * @author ruoyi
 * @date 2024-06-21
 */
@RestController
@RequestMapping("/admin/recharge")
public class TtFirstRechargeController extends BaseController
{
    @Autowired
    private TtFirstRechargeService ttFirstRechargeService;

    /**
     * 查询首充赠送列表
     */
    @PreAuthorize("@ss.hasPermi('admin:recharge:list')")
    @GetMapping("/list")
    public TableDataInfo list(TtFirstRecharge ttFirstRecharge)
    {
        startPage();
        List<TtFirstRecharge> list = ttFirstRechargeService.selectTtFirstRechargeList(ttFirstRecharge);
        return getDataTable(list);
    }

    /**
     * 导出首充赠送列表
     */
    @PreAuthorize("@ss.hasPermi('admin:recharge:export')")
    @Log(title = "首充赠送", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TtFirstRecharge ttFirstRecharge)
    {
        List<TtFirstRecharge> list = ttFirstRechargeService.selectTtFirstRechargeList(ttFirstRecharge);
        ExcelUtil<TtFirstRecharge> util = new ExcelUtil<TtFirstRecharge>(TtFirstRecharge.class);
        util.exportExcel(response, list, "首充赠送数据");
    }

    /**
     * 获取首充赠送详细信息
     */
    @PreAuthorize("@ss.hasPermi('admin:recharge:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Integer id)
    {
        return success(ttFirstRechargeService.selectTtFirstRechargeById(id));
    }

    /**
     * 新增首充赠送
     */
    @PreAuthorize("@ss.hasPermi('admin:recharge:add')")
    @Log(title = "首充赠送", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TtFirstRecharge ttFirstRecharge)
    {
        return toAjax(ttFirstRechargeService.insertTtFirstRecharge(ttFirstRecharge));
    }

    /**
     * 修改首充赠送
     */
    @PreAuthorize("@ss.hasPermi('admin:recharge:edit')")
    @Log(title = "首充赠送", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TtFirstRecharge ttFirstRecharge)
    {
        return toAjax(ttFirstRechargeService.updateTtFirstRecharge(ttFirstRecharge));
    }

    /**
     * 删除首充赠送
     */
    @PreAuthorize("@ss.hasPermi('admin:recharge:remove')")
    @Log(title = "首充赠送", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids)
    {
        return toAjax(ttFirstRechargeService.deleteTtFirstRechargeByIds(ids));
    }
}
