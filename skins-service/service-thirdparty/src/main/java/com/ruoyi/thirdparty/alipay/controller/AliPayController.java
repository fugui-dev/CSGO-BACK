package com.ruoyi.thirdparty.alipay.controller;

import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.annotation.UserPermission;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.CreateOrderParam;
import com.ruoyi.thirdparty.alipay.service.AliPayService;
import com.ruoyi.thirdparty.wechat.service.ApiTokenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "支付宝")
@RestController
@RequestMapping("/api/alipay")
public class AliPayController {

    private final ApiTokenService tokenService;
    private final AliPayService aliPayService;
    private final TtUserService ttUserService;

    public AliPayController(ApiTokenService apiTokenService,
                            AliPayService aliPayService,
                            TtUserService ttUserService) {
        this.tokenService = apiTokenService;
        this.aliPayService = aliPayService;
        this.ttUserService = ttUserService;
    }

    @ApiOperation("支付")
    @UserPermission
    @PostMapping(value = "/trade/page/pay")
    public R pay(@RequestBody @Validated CreateOrderParam param) {
        Long userId = SecurityUtils.getUserId();
        TtUser ttUser = ttUserService.getById(userId);
        // 是否实名认证（0未认证 1已认证）
        if ("0".equals(ttUser.getIsRealCheck())) {
            return R.fail("未实名认证");
        }
        String ip = IpUtils.getIpAddr();
        return aliPayService.pay(param, ttUser, ip);
    }

    @PostMapping(value = "/callBack")
    public String callBack(@RequestParam Map<String, String> params) {
        return aliPayService.callBack(params);
    }
}
