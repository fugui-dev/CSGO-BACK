package com.ruoyi.user.controller;

import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.annotation.UpdateUserCache;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.user.service.ApiBonusService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "奖励补贴")
@RestController
@RequestMapping("/api/bonus")
public class ApiBonusController extends BaseController {

    private final ISysConfigService sysConfigService;
    private final ApiBonusService apiBonusService;
    private final TtUserService userService;

    public ApiBonusController(ISysConfigService sysConfigService,
                              ApiBonusService apiBonusService,
                              TtUserService userService) {
        this.sysConfigService = sysConfigService;
        this.apiBonusService = apiBonusService;
        this.userService = userService;
    }

    @ApiOperation("领取红包")
    @UpdateUserCache
    @GetMapping("/receiveRedPacket/{code}")
    public R receiveRedPacket(@PathVariable("code") String code) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        TtUser ttUser = userService.getById(getUserId());
        return apiBonusService.receiveRedPacket(code, ttUser);
    }
}
