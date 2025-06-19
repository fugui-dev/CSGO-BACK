package com.ruoyi.admin.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.domain.entity.TtPayConfig;
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
import com.ruoyi.admin.service.ITtPayConfigService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 支付配置Controller
 * 
 * @author ruoyi
 * @date 2024-06-25
 */
@RestController
@RequestMapping("/payConfig/payConfig")
public class TtPayConfigController extends BaseController
{
    @Autowired
    private ITtPayConfigService ttPayConfigService;

    /**
     * 查询支付配置列表
     */
    @PreAuthorize("@ss.hasPermi('payConfig:payConfig:list')")
    @GetMapping("/list")
    public TableDataInfo list(TtPayConfig ttPayConfig)
    {
        startPage();
        List<TtPayConfig> list = ttPayConfigService.selectTtPayConfigList(ttPayConfig);
        return getDataTable(list);
    }

    /**
     * 导出支付配置列表
     */
    @PreAuthorize("@ss.hasPermi('payConfig:payConfig:export')")
    @Log(title = "支付配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TtPayConfig ttPayConfig)
    {
        List<TtPayConfig> list = ttPayConfigService.selectTtPayConfigList(ttPayConfig);
        ExcelUtil<TtPayConfig> util = new ExcelUtil<TtPayConfig>(TtPayConfig.class);
        util.exportExcel(response, list, "支付配置数据");
    }

    /**
     * 获取支付配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('payConfig:payConfig:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(ttPayConfigService.selectTtPayConfigById(id));
    }

    /**
     * 新增支付配置
     */
    @PreAuthorize("@ss.hasPermi('payConfig:payConfig:add')")
    @Log(title = "支付配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TtPayConfig ttPayConfig)
    {
        return toAjax(ttPayConfigService.insertTtPayConfig(ttPayConfig));
    }

    /**
     * 修改支付配置
     */
    @PreAuthorize("@ss.hasPermi('payConfig:payConfig:edit')")
    @Log(title = "支付配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TtPayConfig ttPayConfig)
    {
        return toAjax(ttPayConfigService.updateTtPayConfig(ttPayConfig));
    }

    /**
     * 删除支付配置
     */
    @PreAuthorize("@ss.hasPermi('payConfig:payConfig:remove')")
    @Log(title = "支付配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(ttPayConfigService.deleteTtPayConfigByIds(ids));
    }
}
