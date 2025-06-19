package com.ruoyi.promo.service.impl;

import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.ruoyi.admin.mapper.TtUserMapper;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.framework.web.service.SysLoginService;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.promo.service.PromoLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PromoLoginServiceImpl implements PromoLoginService {

    @Autowired
    private SysLoginService sysLoginService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TtUserMapper ttUserMapper;

    @Override
    public String login(String username, String password) {
        // 登录前置校验
        sysLoginService.loginPreCheck(username, password);
        // 将前缀提供给ApiUserDetailsServiceImpl.loadUserByUsername，只允许运营类型用户登录
        LoginUser loginUser = sysLoginService.createLoginUser("promo_" + username, password);
        // 记录登录信息
        recordLoginInfo(loginUser.getUserId());
        return tokenService.createToken(loginUser);
    }

    /**
     * 记录登录信息
     */
    public void recordLoginInfo(Long userId) {
        new LambdaUpdateChainWrapper<>(ttUserMapper).eq(TtUser::getUserId, userId)
                .set(TtUser::getLoginIp, IpUtils.getIpAddr())
                .set(TtUser::getLoginDate, DateUtils.getNowDate()).update();
    }
}
