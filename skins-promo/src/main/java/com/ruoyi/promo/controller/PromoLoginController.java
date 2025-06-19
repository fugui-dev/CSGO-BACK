package com.ruoyi.promo.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginBody;
import com.ruoyi.domain.other.ApiLoginBody;
import com.ruoyi.promo.service.PromoLoginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "推广后台用户认证")
@RestController
@RequestMapping("/promo")
public class PromoLoginController {

    @Autowired
    private PromoLoginService promoLoginService;

    @ApiOperation("推广后台用户登录")
    @Anonymous
    @PostMapping("/login")
    public AjaxResult login(@RequestBody ApiLoginBody apiLoginBody) {
        AjaxResult ajax = AjaxResult.success();
        String token = promoLoginService.login(apiLoginBody.getUsername(), apiLoginBody.getPassword());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }
}
