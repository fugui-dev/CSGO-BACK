package com.ruoyi.thirdparty.wechat.controller;

import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.annotation.UserPermission;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.thirdparty.wechat.domain.PayOrderParam;
import com.ruoyi.thirdparty.wechat.service.ApiTokenService;
import com.ruoyi.thirdparty.wechat.service.TianXinService;
import com.ruoyi.thirdparty.wechat.utils.GetIpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static com.ruoyi.common.utils.SecurityUtils.getUserId;

@Api(tags = "田心支付")
@RestController
@RequestMapping("/api/tianxinOrder")
public class TianXinController {

    private final ApiTokenService tokenService;
    private final TianXinService tianXinService;
    private final TtUserService ttUserService;

    public TianXinController(ApiTokenService tokenService,
                             TianXinService tianXinService,
                             TtUserService ttUserService) {
        this.tokenService = tokenService;
        this.tianXinService = tianXinService;
        this.ttUserService = ttUserService;
    }

    @ApiOperation("预下单")
    @UserPermission
    @PostMapping("/createOrder")
    public AjaxResult createOrder(@RequestBody PayOrderParam param, HttpServletRequest request) {

        // 判断是否开启田心支付
        // if (!("true".equals(sysConfigService.selectConfigByKey("tianxinpay")))) {
        //     return AjaxResult.error("当前系统没有开启田心支付！");
        // }

        return AjaxResult.success("尚未开放。");

        // TtUser ttUser = ttUserService.getById(getUserId());
        // // 是否实名认证0未认证，1已认证'
        // if (ttUser.getIsRealCheck().equals("0")) {
        //     return AjaxResult.error("请实名认证后再进行充值！");
        // }
        //
        // String ip = GetIpUtil.getIpAddress(request);
        // return tianXinService.createOrder(param, ttUser, ip);
    }

    @ApiOperation("查询订单状态")
    @UserPermission
    @PostMapping(value = "/queryOrderStatus")
    public AjaxResult queryOrderStatus(String sdorderno) {
        return tianXinService.queryOrderStatus(sdorderno);
    }

    @ApiOperation("回调")
    @RequestMapping(value = "/callBack")
    public String returnCallBack(HttpServletRequest request) {
        return tianXinService.callBack(request);
    }

}