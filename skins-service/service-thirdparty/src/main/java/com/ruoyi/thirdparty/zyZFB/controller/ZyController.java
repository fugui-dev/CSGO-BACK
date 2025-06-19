package com.ruoyi.thirdparty.zyZFB.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.annotation.UpdateUserCache;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.other.CreateOrderParam;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.thirdparty.zyZFB.config.ZYConfig;
import com.ruoyi.thirdparty.zyZFB.service.zyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(tags = "中云支付宝")
@Slf4j
@RestController
@RequestMapping("api/zyZFB")
public class ZyController extends BaseController {

    @Autowired
    private ZYConfig zyConfig;

    @Autowired
    private zyService zyService;

    @Autowired
    private TtUserService userService;

    // 下单
    @ApiOperation("预下单接口")
    @PostMapping("/ApiAddTrans")
    @UpdateUserCache
    public R ApiAddTrans(HttpServletRequest request, CreateOrderParam param) {
        R checkLogin = checkLogin();
        if (!checkLogin.getCode().equals(200)) return checkLogin;
        Integer userId = ((Long) checkLogin.getData()).intValue();

        TtUser user = userService.getById(userId);

        return zyService.ApiAddTrans(param, user, request);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class PayNotifyData {
        // 支付方式
        private String tranType;
        private String respCode;
        private String respDesc;
        // 交易流水号
        private String serverRspNo;
        private String amt;
        private String merReqNo;
        private String msgExt;
        private String tranTime;
        private String sign;
    }

    // 支付回调
    @ApiOperation("支付回调")
    @PostMapping("/addTransNotify")
    public String addTransNotify(PayNotifyData data) {
        log.info("支付回调 {}", JSONUtil.toJsonStr(data));
        return zyService.payNotify(data);
    }

    // 查询订单接口
    @ApiOperation("查询订单")
    @PostMapping("/ApiQueryTrans")
    public String ApiQueryTrans(HttpServletRequest request, Map<String, String> param) {
        return "success";
    }

    // 代付接口
    @ApiOperation("代付")
    @PostMapping("/ApiPropayTrans")
    public String ApiPropayTrans(HttpServletRequest request, Map<String, String> param) {
        return "success";
    }

    // 查询余额接口
    @ApiOperation("查询余额")
    @PostMapping("/ApiQueryBalancePHP")
    public String ApiQueryBalancePHP(HttpServletRequest request, Map<String, String> param) {
        return "success";
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

    // 支付回调
    // @PostMapping("/propayTransNotify")
    // public String propayTransNotify(Map<String, String> map) {
    //     log.info("代付回调 {}", JSONUtil.toJsonStr(map));
    //     return "success";
    // }
}
