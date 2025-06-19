package com.ruoyi.admin.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.domain.entity.TbPromotionChannel;
import com.ruoyi.admin.service.ITbPromotionChannelService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 推广渠道通道Controller
 * 
 * @author ruoyi
 * @date 2024-06-29
 */
@RestController
@RequestMapping("/promotion/channel")
public class TbPromotionChannelController extends BaseController
{
    @Autowired
    private ITbPromotionChannelService tbPromotionChannelService;

    /**
     * 查询推广渠道通道列表
     */
    @PreAuthorize("@ss.hasPermi('promotion:channel:list')")
    @GetMapping("/list")
    public TableDataInfo list(TbPromotionChannel tbPromotionChannel)
    {
        startPage();
        List<TbPromotionChannel> list = tbPromotionChannelService.selectTbPromotionChannelList(tbPromotionChannel);
        return getDataTable(list);
    }

    /**
     * 导出推广渠道通道列表
     */
    @PreAuthorize("@ss.hasPermi('promotion:channel:export')")
    @Log(title = "推广渠道通道", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TbPromotionChannel tbPromotionChannel)
    {
        List<TbPromotionChannel> list = tbPromotionChannelService.selectTbPromotionChannelList(tbPromotionChannel);
        ExcelUtil<TbPromotionChannel> util = new ExcelUtil<TbPromotionChannel>(TbPromotionChannel.class);
        util.exportExcel(response, list, "推广渠道通道数据");
    }

    /**
     * 获取推广渠道通道详细信息
     */
    @PreAuthorize("@ss.hasPermi('promotion:channel:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tbPromotionChannelService.selectTbPromotionChannelById(id));
    }

    /**
     * 新增推广渠道通道
     */
    @PreAuthorize("@ss.hasPermi('promotion:channel:add')")
    @Log(title = "推广渠道通道", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TbPromotionChannel tbPromotionChannel)
    {
        return toAjax(tbPromotionChannelService.insertTbPromotionChannel(tbPromotionChannel));
    }

    /**
     * 修改推广渠道通道
     */
    @PreAuthorize("@ss.hasPermi('promotion:channel:edit')")
    @Log(title = "推广渠道通道", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TbPromotionChannel tbPromotionChannel)
    {
        return toAjax(tbPromotionChannelService.updateTbPromotionChannel(tbPromotionChannel));
    }

    /**
     * 删除推广渠道通道
     */
    @PreAuthorize("@ss.hasPermi('promotion:channel:remove')")
    @Log(title = "推广渠道通道", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tbPromotionChannelService.deleteTbPromotionChannelByIds(ids));
    }


    /**
     * 获取报表数据
     */
    @PreAuthorize("@ss.hasPermi('promotion:channel:query')")
    @GetMapping(value = "/reportInfo")
    public AjaxResult reportInfo(@RequestParam("id") Integer id)
    {
        return tbPromotionChannelService.reportInfo(id);
    }

}
