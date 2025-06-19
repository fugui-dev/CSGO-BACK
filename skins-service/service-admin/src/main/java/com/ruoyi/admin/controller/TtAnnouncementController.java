package com.ruoyi.admin.controller;

import com.ruoyi.admin.service.TtAnnouncementService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.domain.other.TtAnnouncement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/admin/announcement")
public class TtAnnouncementController extends BaseController {

    @Autowired
    private TtAnnouncementService ttAnnouncementService;

    /**
     * 获取公告列表
     */
    @GetMapping("/list")
    public PageDataInfo<TtAnnouncement> list() {
        startPage();
        List<TtAnnouncement> announcementList = ttAnnouncementService.getAnnouncementList();
        return getPageData(announcementList);
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{announcementId}")
    public AjaxResult getAnnouncementByAnnouncementId(@PathVariable Integer announcementId) {
        Long userId = SecurityUtils.getUserId();
        TtAnnouncement ttAnnouncement = ttAnnouncementService.getAnnouncementByAnnouncementId(userId, announcementId);
        return AjaxResult.success(ttAnnouncement);
    }

    /**
     * 新增公告
     */
    @PostMapping
    public AjaxResult addAnnouncement(@RequestBody TtAnnouncement ttAnnouncement) {
        return ttAnnouncementService.addAnnouncement(ttAnnouncement) > 0 ? AjaxResult.success("新增成功") : AjaxResult.error("新增失败");
    }

    /**
     * 修改公告
     */
    @PutMapping
    public AjaxResult editAnnouncement(@RequestBody TtAnnouncement ttAnnouncement) {
        return ttAnnouncementService.editAnnouncement(ttAnnouncement) > 0 ? AjaxResult.success("修改成功") : AjaxResult.error("修改失败");
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{announcementId}")
    public AjaxResult removeAnnouncementByAnnouncementId(@PathVariable Integer announcementId) {
        return ttAnnouncementService.removeAnnouncementByAnnouncementId(announcementId) > 0 ? AjaxResult.success("删除成功") : AjaxResult.error("删除失败");
    }
}
