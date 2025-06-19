package com.ruoyi.thirdparty.jiujia.controller;

import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.thirdparty.jiujia.domain.CallbackBody;
import com.ruoyi.thirdparty.jiujia.service.JiuJiaPayService;
import com.ruoyi.domain.other.CreateOrderParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "九嘉支付")
@RestController
@RequestMapping("/api/jiuJiaPay")
public class JiuJiaPayController extends BaseController {

    private final ISysConfigService sysConfigService;
    private final TtUserService userService;
    private final JiuJiaPayService jiuJiaPayService;

    public JiuJiaPayController(ISysConfigService sysConfigService,
                               TtUserService userService,
                               JiuJiaPayService jiuJiaPayService) {
        this.sysConfigService = sysConfigService;
        this.userService = userService;
        this.jiuJiaPayService = jiuJiaPayService;
    }

    // TODO: 2024/4/11 要修改！！！
    @ApiOperation("回滚")
    @Anonymous
    @PostMapping("/callback")
    public String callback(@RequestBody CallbackBody callbackBody) {
        return jiuJiaPayService.callback(callbackBody);
    }

    @ApiOperation("九嘉预下单")
    @PostMapping("/createPay")
    public R<Object> createPay(@RequestBody CreateOrderParam createOrderBody) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        TtUser ttUser = userService.getById(getUserId());
        String msg = jiuJiaPayService.createPay(createOrderBody, ttUser);
        return msg.startsWith("https") ? R.ok(msg) : R.fail(msg);
    }

}
