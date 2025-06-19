package com.ruoyi.user.controller;


import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.domain.other.TtAnnouncement;
import com.ruoyi.user.service.ApiAnnouncementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@Api(tags = "公告")
@RestController
@RequestMapping("/api/announcement")
public class ApiAnnouncementController extends BaseController {

    @Autowired
    private ApiAnnouncementService apiAnnouncementService;

    @ApiOperation("获取公告列表")
    @ApiResponse(code = 200, message = "read【阅读状态（0未读 1已读）】")
    @GetMapping("/list")
    public TableDataInfo list() {
        Long userId = SecurityUtils.getUserId();
        startPage();
        List<TtAnnouncement> announcementList = apiAnnouncementService.getAnnouncementList(userId);
        return getDataTable(announcementList);
    }

    @ApiOperation("获取公告详情")
    @GetMapping("/{announcementId}")
    public AjaxResult getAnnouncementByAnnouncementId(@PathVariable Integer announcementId) {
        Long userId = SecurityUtils.getUserId();
        TtAnnouncement ttAnnouncement = apiAnnouncementService.getAnnouncementByAnnouncementId(announcementId, userId);
        if (Objects.isNull(ttAnnouncement)) {
            return AjaxResult.error("公告不存在");
        }
        return AjaxResult.success(ttAnnouncement);
    }

    @ApiOperation("获取未读公告数量")
    @GetMapping("/countUnreadAnnouncement")
    public AjaxResult countUnreadAnnouncement() {
        Long userId = SecurityUtils.getUserId();
        return AjaxResult.success(apiAnnouncementService.countUnreadAnnouncement(userId));
    }
}
