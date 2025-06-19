package com.ruoyi.thirdparty.common.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.admin.service.TtRechargeProdService;
import com.ruoyi.domain.entity.TtRechargeProd;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.annotation.UpdateUserCache;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.thirdparty.common.service.ApiRechargeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "充值")
@RestController
@RequestMapping("/api/recharge")
public class ApiRechargeController extends BaseController {

    private final ISysConfigService sysConfigService;
    private final TtUserService userService;
    private final ApiRechargeService apiRechargeService;
    private final TtRechargeProdService rechargeListService;

    public ApiRechargeController(ISysConfigService sysConfigService,
                                 TtUserService userService,
                                 ApiRechargeService apiRechargeService,
                                 TtRechargeProdService rechargeListService) {
        this.sysConfigService = sysConfigService;
        this.userService = userService;
        this.apiRechargeService = apiRechargeService;
        this.rechargeListService = rechargeListService;
    }

    @ApiOperation("卡支付")
    @UpdateUserCache
    @PostMapping("/cardPay")
    public R<Boolean> cardPay(String password) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        TtUser ttUser = userService.getById(getUserId());
        String msg = apiRechargeService.cardPay(password, ttUser);
        return StringUtils.isEmpty(msg) ? R.ok(true, "充值成功！") : R.fail(false, msg);
    }

    @ApiOperation("充值列表")
    // @UpdateUserCache
    @GetMapping("/list")
    public R<List<TtRechargeProd>> list() {
        startPage();
        LambdaQueryWrapper<TtRechargeProd> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(TtRechargeProd::getStatus, "0");
        List<TtRechargeProd> list = rechargeListService.list(wrapper);
        return R.ok(list);
    }
}
