package com.ruoyi.user.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.user.model.TtTaskCenterUser;
import com.ruoyi.user.model.vo.ApiTaskCenterVO;
import com.ruoyi.user.service.ApiTaskCenterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@Api(tags = "任务中心")
@RestController
@RequestMapping("/api/taskCenter")
public class ApiTaskCenterController extends BaseController {

    @Autowired
    private ApiTaskCenterService apiTaskCenterService;

    @ApiOperation("查询任务列表")
    @ApiResponse(code = 200, message = "status【领取条件（0不满足 1满足）】" +
            "<br>claimed【是否已领取（1已领取）】")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    public TableDataInfo list() {
        Long userId = getUserId();
        startPage();
        List<ApiTaskCenterVO> list = apiTaskCenterService.selectApiTaskCenterVOList(userId);
        return getDataTable(list);
    }

    @ApiOperation("领取任务奖励")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/getReward/{taskId}")
    public AjaxResult getReward(@PathVariable("taskId") Integer taskId) {
        Long userId = getUserId();
        String type = apiTaskCenterService.selectTaskTypeByTaskId(taskId);
        // 判断是否具备领取条件
        TtTaskCenterUser ttTaskCenterUser = apiTaskCenterService.selectTtTaskCenterUserByUserIdAndType(userId, type);
        if (Objects.isNull(ttTaskCenterUser)) {
            return AjaxResult.error("不具备领取条件");
        }
        if ("1".equals(ttTaskCenterUser.getClaimed())) {
            return AjaxResult.error("已领取过");
        }
        return apiTaskCenterService.getReward(userId, type);
    }
}
