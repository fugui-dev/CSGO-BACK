package com.ruoyi.thirdparty.decsgopay.csgopay.controller;

import cn.hutool.core.util.ObjectUtil;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.UpdateUserCache;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.CreateOrderParam;
import com.ruoyi.thirdparty.decsgopay.csgopay.service.CSPayService;
import com.ruoyi.thirdparty.unifypaycallbackprocess.UnifyCallbackProcess;
import com.ruoyi.thirdparty.zhaocaipay.vo.UnifyPayPreOrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@Api(tags = "csPay支付")
@Slf4j
@RestController
@RequestMapping("api/csPay")
public class CSPayController extends BaseController {

    @Autowired
    TtUserService userService;

    @Autowired
    CSPayService csPayService;

    @Autowired
    UnifyCallbackProcess unifyCallbackProcess;

    @ApiOperation("订单状态接口，支付结果轮询")
    @GetMapping("/orderStatus")
    @UpdateUserCache
    public R<Boolean> orderStatus(@RequestParam("orderNo") String orderNo) {
        return unifyCallbackProcess.orderStatus(orderNo);
    }


    // 下单
    @ApiOperation("预下单")
    @PostMapping("/preOrder")
    @UpdateUserCache
    public R<UnifyPayPreOrderVO> preOrder(HttpServletRequest request, @RequestBody @Validated CreateOrderParam param) {
        R checkLogin = checkLogin();
        if (!checkLogin.getCode().equals(200)) return checkLogin;
        Integer userId = ((Long) checkLogin.getData()).intValue();

        TtUser user = userService.getById(userId);
        // 是否实名认证0未认证，1已认证'
        if (user.getIsRealCheck().equals("0")) {
            return R.fail("请实名认证后再进行充值！");
        }

        return csPayService.preOrder(param, user, request);
    }

    @ApiOperation("支付回调")
    @Anonymous
    @GetMapping("/notify")
    public String notify(HttpServletRequest request) {

        //处理回调
        try {
            return csPayService.callback(request);

        }catch (Exception e){
            return "fail";

        }

    }

        public R checkLogin() {
        Long userId;
        try {
            userId = getUserId();
            if (ObjectUtil.isEmpty(userId)) return R.fail(401,"登录过期，请重新登录。");
            return R.ok(userId);
        } catch (Exception e) {
            return R.fail("登录过期，请重新登录。");
        }
    }



    @ApiOperation("支付回调")
    @Anonymous
    @PostMapping("/testNotify")
    public String testNotify(String testOrderNo) {
        //处理回调
        try {
            return "csPayService.testCallbackProcess(testOrderNo)";

        }catch (Exception e){
            return "fail";

        }
    }

    }
