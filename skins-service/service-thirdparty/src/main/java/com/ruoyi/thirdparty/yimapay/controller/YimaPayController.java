package com.ruoyi.thirdparty.yimapay.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.annotation.UpdateUserCache;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.dto.yima.YimaPayNotifyRequest;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.CreateOrderParam;
import com.ruoyi.thirdparty.MaYi.utils.signUtil;
import com.ruoyi.thirdparty.yimapay.config.YimaConfig;
import com.ruoyi.thirdparty.yimapay.service.YimaService;
import com.ruoyi.thirdparty.zhaocaipay.vo.UnifyPayPreOrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/*
 * @description
 * @date 2025/6/12
 */
@Api(tags = "yimaPay支付")
@Slf4j
@RestController
@RequestMapping("api/yimaPay")
public class YimaPayController extends BaseController {
    @Autowired
    YimaConfig yimaConfig;

    @Autowired
    TtUserService userService;

    @Autowired
    YimaService yimaService;

    // 下单
    @ApiOperation("预下单")
    @PostMapping("/preOrder")
    @UpdateUserCache
    public synchronized R<UnifyPayPreOrderVO> preOrder(HttpServletRequest request, @RequestBody @Validated CreateOrderParam param) {
        R checkLogin = checkLogin();
        if (!checkLogin.getCode().equals(200)) return checkLogin;
        Integer userId = ((Long) checkLogin.getData()).intValue();

        TtUser user = userService.getById(userId);
        // 是否实名认证0未认证，1已认证'
        if (user.getIsRealCheck().equals("0")) {
            return R.fail("请实名认证后再进行充值！");
        }
        return yimaService.preOrder(param, user, request);
    }

    // 支付回调
    @SneakyThrows
    @ApiOperation("支付回调")
    @PostMapping("/notify")
    public Map<String,String> addTransNotify(HttpServletRequest request) {
        Map<String, String> notifyDto = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues != null && paramValues.length > 0) {
                String paramValue = URLDecoder.decode(paramValues[0], "utf-8");
                notifyDto.put(paramName, paramValue);
            }
        }
        YimaPayNotifyRequest yimaPayNotifyRequest = BeanUtil.mapToBean(notifyDto, YimaPayNotifyRequest.class,true);
        log.info("yima聚合支付支付回调 {}", JSONUtil.toJsonStr(yimaPayNotifyRequest));
//        String sign = signUtil.getSignOfYima(notifyDto, null, yimaConfig.getApiKey());
        /*log.info("生成签名：{}", sign);
        // sign: 1C548013B5DB543B7728374D41E2504C
        if (!sign.equals(notifyDto.get("sign"))) {
            Map<String, String> ret = new HashMap<>();
            ret.put("code", "FAIL");
            ret.put("message", "签名错误");
            return ret;
        }*/
        String code = yimaService.payNotify(yimaPayNotifyRequest);
        Map<String,String> map = new HashMap<>();
        map.put("code",code);
        map.put("message","");
        return map;
    }


    @ApiOperation("订单状态接口，支付结果轮询")
    @GetMapping("/orderStatus")
    @UpdateUserCache
    public R<Boolean> orderStatus(@RequestParam("orderNo") String orderNo) {
        return yimaService.orderStatus(orderNo);
    }

    public R checkLogin() {
        Long userId;
        try {
            userId = getUserId();
            if (ObjectUtil.isEmpty(userId)) return R.fail(401, "登录过期，请重新登录。");
            return R.ok(userId);
        } catch (Exception e) {
            return R.fail("登录过期，请重新登录。");
        }
    }
}
