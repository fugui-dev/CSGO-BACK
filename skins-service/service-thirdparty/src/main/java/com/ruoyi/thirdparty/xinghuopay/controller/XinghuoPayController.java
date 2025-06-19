package com.ruoyi.thirdparty.xinghuopay.controller;

import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.annotation.UserPermission;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.CreateOrderParam;
import com.ruoyi.thirdparty.xinghuopay.service.XinghuoPayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "XinghuoPay")
@Slf4j
@RestController
@RequestMapping("api/xinghuopay")
public class XinghuoPayController extends BaseController {

    @Autowired
    private TtUserService ttUserService;

    @Autowired
    private XinghuoPayService xinghuoService;

    @ApiOperation("支付")
    @UserPermission
    @PostMapping(value = "/pay")
    public R pay(@RequestBody @Validated CreateOrderParam param) {
        Long userId = SecurityUtils.getUserId();
        TtUser ttUser = ttUserService.getById(userId);
        // 是否实名认证（0未认证 1已认证）
        if ("0".equals(ttUser.getIsRealCheck())) {
            return R.fail("未实名认证");
        }
        String ip = IpUtils.getIpAddr();
        return xinghuoService.pay(param, ttUser, ip);
    }

    @GetMapping("/notify")
    public String notify(@RequestParam Map<String, String> params) {
        return xinghuoService.notify(params);
    }
}
