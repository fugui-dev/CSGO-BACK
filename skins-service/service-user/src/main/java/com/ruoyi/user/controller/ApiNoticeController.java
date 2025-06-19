package com.ruoyi.user.controller;


import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.domain.other.TtNotice;
import com.ruoyi.user.model.vo.ApiNoticeVO;
import com.ruoyi.user.service.ApiNoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Api(tags = "通知")
@RestController
@RequestMapping("/api/notice")
public class ApiNoticeController extends BaseController {

    @Autowired
    private ApiNoticeService apiNoticeService;

    @ApiOperation("获取通知列表")
    @ApiResponse(code = 200, message = "read【阅读状态（0未读 1已读）】")
    @GetMapping("/list")
    public TableDataInfo list() {
        Long userId = SecurityUtils.getUserId();
        startPage();
        List<ApiNoticeVO> apiNoticeVOList = apiNoticeService.getNoticeList(userId);
        return getDataTable(apiNoticeVOList);
    }

    @ApiOperation("获取通知详情")
    @GetMapping("/{noticeId}")
    public AjaxResult getNoticeByNoticeId(@PathVariable Integer noticeId) {
        Long userId = SecurityUtils.getUserId();
        ApiNoticeVO apiNoticeVO = apiNoticeService.getNoticeByNoticeId(userId, noticeId);
        if (Objects.isNull(apiNoticeVO)) {
            return AjaxResult.error("通知不存在");
        }
        return AjaxResult.success(apiNoticeVO);
    }

    @ApiOperation("获取未读通知数量")
    @GetMapping("/countUnreadNotice")
    public AjaxResult countUnreadNotice() {
        Long userId = SecurityUtils.getUserId();
        return AjaxResult.success(apiNoticeService.countUnreadNotice(userId));
    }
}
