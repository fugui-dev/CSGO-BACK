package com.ruoyi.user.controller;

import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.NewUserInfo;
import com.ruoyi.common.annotation.UpdateUserCache;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.entity.UserData;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.ApiVerificationCodeLoginBody;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.user.service.ApiLoginService;
import com.ruoyi.domain.other.ApiLoginBody;
import com.ruoyi.user.service.ApiUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 登录Controller
 */
@Api(tags = "客户端用户认证")
@RestController
@RequestMapping("/api")
public class ApiLoginController extends BaseController {

    private final ISysConfigService sysConfigService;
    private final ApiLoginService loginService;
    private final TokenService tokenService;

    public ApiLoginController(ISysConfigService sysConfigService,
                              ApiLoginService loginService,
                              TokenService tokenService) {
        this.sysConfigService = sysConfigService;
        this.loginService = loginService;
        this.tokenService = tokenService;
    }

    @Autowired
    private ApiUserService apiUserService;

    @Autowired
    private TtUserService userService;

    @ApiOperation("客户端用户登录")
    @Anonymous
    @PostMapping("/login")
    public AjaxResult login(@RequestBody @Validated ApiLoginBody loginBody) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return AjaxResult.error("网站维护中......");
        }
        AjaxResult ajax = AjaxResult.success();
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    @ApiOperation("客户端用户验证码登录")
    @Anonymous
    @PostMapping("/verificationCodeLogin")
    public AjaxResult verificationCodeLogin(@RequestBody @Validated ApiVerificationCodeLoginBody apiVerificationCodeLoginBody) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return AjaxResult.error("网站维护中......");
        }
        return loginService.verificationCodeLogin(apiVerificationCodeLoginBody.getPhoneNumber(), apiVerificationCodeLoginBody.getCode());
    }

    @ApiOperation("获取用户信息")
    @NewUserInfo
    @GetMapping("/getInfo")
    public R<UserData> getInfo() {
        UserData userData = getLoginUser().getUserData();
        TtUser ttUser = userService.getById(getUserId());
        apiUserService.authenticationOk(ttUser);
        if (!"1".equals(userData.getIsRealCheck())) {
            userData.setRealName("");
        }
        return R.ok(userData);
    }

    @ApiOperation("客户端用户退出")
    @PostMapping("/logout")
    public R<Object> logout() {
        if (StringUtils.isNotNull(getLoginUser())) {
            String userName = getUsername();
            tokenService.delLoginUser(getToken());
            AsyncManager.me().execute(AsyncFactory.recordLogininfor("api_" + userName, Constants.LOGOUT, "退出成功"));
        }
        return R.ok("退出成功！");
    }

}
