package com.ruoyi.thirdparty.qspay.controller;

import cn.hutool.core.util.ObjectUtil;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.UpdateUserCache;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.CreateOrderParam;
import com.ruoyi.thirdparty.qspay.process.QsPayProcess;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@Api(tags = "QS支付")
@Slf4j
@RestController
@RequestMapping("api/qsPay")
public class QsPayController extends BaseController {


    @Autowired
    QsPayProcess qsPayProcess;

    @Autowired
    TtUserService userService;

    // 下单
    @ApiOperation("预下单")
    @PostMapping("/preOrder")
    @UpdateUserCache
    public R ApiAddTrans(HttpServletRequest request, @RequestBody @Validated CreateOrderParam param) {
        if (StringUtils.isBlank(param.getPayType())){
//            return R.fail("请选择支付方式！");
            param.setPayType("alipay");
        }

        R checkLogin = checkLogin();
        if (!checkLogin.getCode().equals(200)) return checkLogin;
        Integer userId = ((Long) checkLogin.getData()).intValue();

        TtUser user = userService.getById(userId);
        // 是否实名认证0未认证，1已认证'
        if (user.getIsRealCheck().equals("0")) {
            return R.fail("请实名认证后再进行充值！");
        }
        return qsPayProcess.preOrder(param, user, request);
    }



    @ApiOperation("支付回调")
    @Anonymous
    @GetMapping("/notify")
    public String addTransNotify(HttpServletRequest request){

//        Map<String, String> parameterMap = new HashMap<>();
//        // 获取请求参数的名称
//        for (String paramName : request.getParameterMap().keySet()) {
//            // 将参数名称和对应的值放入Map中
//            parameterMap.put(paramName, Arrays.toString(request.getParameterValues(paramName)));
//        }

        return qsPayProcess.notifyProcess(getParameterMap(request));
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

    public static Map<String, String> getParameterMap(HttpServletRequest request) {
        Map<String, String> parameterMap = new HashMap<>();

        // 获取请求参数的名称
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String paramName = entry.getKey();
            String[] paramValues = entry.getValue();

            // 将参数值数组转换为字符串，如果有多个值，用逗号分隔
            String paramValue = String.join(",", paramValues);
            parameterMap.put(paramName, paramValue);
        }

        return parameterMap;
    }


}
