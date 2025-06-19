package com.ruoyi.thirdparty.MaYi.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.annotation.UpdateUserCache;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.dto.mayi.PayNotifyRequest;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.CreateOrderParam;
import com.ruoyi.thirdparty.MaYi.config.MYConfig;
import com.ruoyi.thirdparty.MaYi.service.MYService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(tags = "聚合支付")
@Slf4j
@RestController
@RequestMapping("api/mayi")
public class MYController extends BaseController {

    @Autowired
    private MYService myService;

    @Autowired
    private TtUserService userService;

    // 下单
    @ApiOperation("预下单")
    @PostMapping("/ApiAddTrans")
    @UpdateUserCache
    public R ApiAddTrans(HttpServletRequest request, @RequestBody @Validated CreateOrderParam param) {
        R checkLogin = checkLogin();
        if (!checkLogin.getCode().equals(200)) return checkLogin;
        Integer userId = ((Long) checkLogin.getData()).intValue();

        TtUser user = userService.getById(userId);
        // 是否实名认证0未认证，1已认证'
        if (user.getIsRealCheck().equals("0")) {
            return R.fail("请实名认证后再进行充值！");
        }
        return myService.ApiAddTrans(param, user, request);
    }

    // 支付回调
    @ApiOperation("支付回调")
    @PostMapping("/notify")
    public String addTransNotify(@RequestBody PayNotifyRequest data) {
        log.info("聚合支付支付回调 {}", JSONUtil.toJsonStr(data));
        return myService.payNotify(data);
    }

    // 查询订单接口
    // @PostMapping("/ApiQueryTrans")
    // public String ApiQueryTrans(HttpServletRequest request, Map<String, String> param) {
    //     return "success";
    // }
    //
    // // 查询余额接口
    // @PostMapping("/ApiQueryBalancePHP")
    // public String ApiQueryBalancePHP(HttpServletRequest request, Map<String, String> param) {
    //     return "success";
    // }

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
}
