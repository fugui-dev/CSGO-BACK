package com.ruoyi.playingmethod.controller;

import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.TtBox;
import com.ruoyi.domain.vo.OpenBoxVO;
import com.ruoyi.playingmethod.model.ApiWelfare;
import com.ruoyi.playingmethod.model.ApiWelfareRecord;
import com.ruoyi.playingmethod.service.ApiWelfareService;
import com.ruoyi.system.service.ISysConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "福利")
@RestController
@RequestMapping("/api/welfare")
public class ApiWelfareController extends BaseController {

    @Autowired
    private ApiWelfareService apiWelfareService;

    @Autowired
    private ISysConfigService sysConfigService;

    @Autowired
    private TtUserService userService;

    @ApiOperation("获取福利列表")
    @ApiResponse(code = 200, message = "eligible【是否具备领取条件（0否 1是）】" +
            "<br>claimStatus【领取状态（0未领取 1已领取）】")
    @GetMapping("/getWelfareList")
    public TableDataInfo getWelfareList() {
        Long userId = SecurityUtils.getUserId();
        startPage();
        List<ApiWelfare> list = apiWelfareService.getWelfareList(userId);
        return getDataTable(list);
    }

    @ApiOperation("领取福利")
    @GetMapping("/claimWelfare/{welfareId}")
    public AjaxResult claimWelfare(@PathVariable Integer welfareId) {
        Long userId = SecurityUtils.getUserId();
        // 检查是否尚未具备领取条件
        if (apiWelfareService.checkNotEligible(welfareId, userId)) {
            return AjaxResult.error("尚未达到领取条件");
        }
        // 检查是否已领取
        if (apiWelfareService.checkClaimed(welfareId, userId)) {
            return AjaxResult.error("该福利已被领取");
        }
        // 进入开箱流程
        String bindBoxMaintenance = sysConfigService.selectConfigByKey("bindBoxMaintenance");
        if ("1".equals(bindBoxMaintenance)) {
            return AjaxResult.error("盲盒开箱功能正在维护中......");
        }
        TtUser ttUser = userService.getById(getUserId());
        if ("0".equals(ttUser.getIsRealCheck())) {
            return AjaxResult.error("您的账户未进行实名认证，请先进行实名认证！");
        }
        OpenBoxVO openBoxVO = apiWelfareService.claimWelfare(welfareId, userId);
        return AjaxResult.success(openBoxVO);
    }
}
