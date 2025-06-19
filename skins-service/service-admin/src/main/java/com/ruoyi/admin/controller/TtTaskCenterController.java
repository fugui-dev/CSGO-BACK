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
import com.ruoyi.domain.other.TtTaskCenter;
import com.ruoyi.admin.service.TtTaskCenterService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 任务中心Controller
 *
 * @author ruoyi
 * @date 2024-05-25
 */
@RestController
@RequestMapping("/admin/taskCenter")
public class TtTaskCenterController extends BaseController
{
    @Autowired
    private TtTaskCenterService ttTaskCenterService;

    /**
     * 查询任务列表
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    public TableDataInfo list(TtTaskCenter ttTaskCenter)
    {
        startPage();
        List<TtTaskCenter> list = ttTaskCenterService.selectTtTaskCenterList(ttTaskCenter);
        return getDataTable(list);
    }

    /**
     * 获取任务详细信息
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getInfo(@PathVariable("taskId") Integer taskId)
    {
        return success(ttTaskCenterService.selectTtTaskCenterByTaskId(taskId));
    }

    /**
     * 新增任务
     */
    @PreAuthorize("isAuthenticated()")
    @Log(title = "任务中心", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TtTaskCenter ttTaskCenter)
    {
        return toAjax(ttTaskCenterService.insertTtTaskCenter(ttTaskCenter));
    }

    /**
     * 修改任务
     */
    @PreAuthorize("isAuthenticated()")
    @Log(title = "任务中心", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TtTaskCenter ttTaskCenter)
    {
        return toAjax(ttTaskCenterService.updateTtTaskCenter(ttTaskCenter));
    }

    /**
     * 删除任务
     */
    @PreAuthorize("isAuthenticated()")
    @Log(title = "任务中心", businessType = BusinessType.DELETE)
    @DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Integer[] taskIds)
    {
        return toAjax(ttTaskCenterService.deleteTtTaskCenterByTaskIds(taskIds));
    }
}
