package com.ruoyi.user.service.impl;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.admin.mapper.TtUserMapper;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.framework.web.service.SysLoginService;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.framework.websocket.WebSocketUsers;
import com.ruoyi.framework.websocket.pojo.ResultData;
import com.ruoyi.thirdparty.common.service.ApiSmsService;
import com.ruoyi.user.service.ApiLoginService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ApiLoginServiceImpl implements ApiLoginService {

    private final ApiSmsService apiSmsService;
    private final TtUserMapper userMapper;
    private final SysLoginService sysLoginService;
    private final TokenService tokenService;
    private final RedisCache redisCache;

    public ApiLoginServiceImpl(ApiSmsService apiSmsService,
                               TtUserMapper userMapper,
                               SysLoginService sysLoginService,
                               TokenService tokenService,
                               RedisCache redisCache) {
        this.apiSmsService = apiSmsService;
        this.userMapper = userMapper;
        this.sysLoginService = sysLoginService;
        this.tokenService = tokenService;
        this.redisCache = redisCache;
    }

    @Override
    public String login(String username, String password) {
        replicatedLogin(username);
        sysLoginService.loginPreCheck(username, password);
        LoginUser loginUser = sysLoginService.createLoginUser("api_" + username, password);
        recordLoginInfo(loginUser.getUserId());
        return tokenService.createToken(loginUser);
    }

    @Override
    public AjaxResult verificationCodeLogin(String phoneNumber, String code) {
        if (StringUtils.isEmpty(phoneNumber)) {
            return AjaxResult.error("手机号不能为空");
        }
        if (!Validator.isMobile(phoneNumber)) {
            return AjaxResult.error("手机号格式错误，请检查手机号是否输入正确！");
        }
        if (StringUtils.isEmpty(code)) {
            return AjaxResult.error("验证码不能为空");
        }
        if (!NumberUtil.isNumber(code) || code.trim().length() != 4) {
            return AjaxResult.error("验证码错误");
        }
        TtUser ttUser = new LambdaQueryChainWrapper<>(userMapper).eq(TtUser::getPhoneNumber, phoneNumber).one();
        if (StringUtils.isNull(ttUser)) {
            return AjaxResult.error("该手机号未在本站注册！");
        }
        String validateCaptcha = apiSmsService.validateCaptcha(code.trim(), "ApiLogin_" + phoneNumber);
//        if (!"success".equals(validateCaptcha)) {
//            return AjaxResult.error(validateCaptcha);
//        }
        String password = ttUser.getRemark().substring(ttUser.getRemark().indexOf(":") + 1);
        String token = this.login(ttUser.getUserName(), password);
        AjaxResult ajax = AjaxResult.success();
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    /**
     * 检测用户是否在其它地方登录，如果存在则发送通知给当前用户，
     * 并执行相应的清理工作，例如删除已登录用户的缓存信息
     */
    private void replicatedLogin(String username) {
        Collection<String> redisKeys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
        List<LoginUser> loginUserList = redisKeys.stream().map(redisCache::<LoginUser>getCacheObject)
                .filter(loginUser -> loginUser.getUser() == null)
                .filter(loginUser -> username.equals(loginUser.getUsername()))
                .collect(Collectors.toList());
        if (!loginUserList.isEmpty()) {
            ResultData<String> resultData = new ResultData<>();
            resultData.setCode(10);
            resultData.setTypeName("success");
            resultData.setData("您的账号在别处登录，如果不是本人操作，请尽快修改密码！");
            WebSocketUsers.sendMessageToUserByText(loginUserList.get(0).getUserId().intValue(),
                    JSON.toJSONString(resultData));
            String token = loginUserList.get(0).getToken();
            redisCache.deleteObject(CacheConstants.LOGIN_TOKEN_KEY + token);
            AsyncManager.me().execute(AsyncFactory.recordLogininfor("api_" + username,
                    Constants.LOGOUT, "退出成功,账号已换机登录！"));
        }
    }

    /**
     * 记录登录信息
     */
    public void recordLoginInfo(Long userId) {
        new LambdaUpdateChainWrapper<>(userMapper).eq(TtUser::getUserId, userId)
                .set(TtUser::getLoginIp, IpUtils.getIpAddr())
                .set(TtUser::getLoginDate, DateUtils.getNowDate()).update();
    }
}
