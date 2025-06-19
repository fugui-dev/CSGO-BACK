package com.ruoyi.framework.aspectj;

import cn.hutool.core.util.ObjectUtil;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.core.domain.entity.UserData;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.framework.web.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Aspect
@Component
@Order(1)
@Slf4j
public class UpdateUserCacheAspect {

    private final TtUserService userService;
    private final TokenService tokenService;

    public UpdateUserCacheAspect(TtUserService userService,
                                 TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @Pointcut("@annotation(com.ruoyi.common.annotation.NewUserInfo)")
    private void NewUserInfo() {
    }

    @Pointcut("@annotation(com.ruoyi.common.annotation.UpdateUserCache)")
    private void permissionCheck() {
    }

    @Before("NewUserInfo()")
    public void NewUserInfo(JoinPoint joinPoint) throws IOException {
        // 获取request对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (StringUtils.isNull(attributes)) return;
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();

        // 获取登录用户信息
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (ObjectUtil.isEmpty(loginUser)){
            response.sendError(401,"登录过期，请重新登录。");
            log.warn("登录过期，请重新登录。");
            return;
        }
        TtUser user = userService.getById(loginUser.getUserId());

        // TODO: 2024/4/18 响应401
        if (ObjectUtil.isEmpty(user)) return;

        // 设置更新后的属性
        UserData userData = loginUser.getUserData();
        // 对象拷贝
        BeanUtils.copyBeanProp(userData, user);
        // 获取上级用户
        TtUser ParentUser = userService.getById(user.getParentId());
        if (StringUtils.isNotNull(ParentUser))
            userData.setParentInvitationCode(ParentUser.getInvitationCode());    // 上级邀请码
        loginUser.setUserData(userData);

        // 刷新token
        tokenService.refreshToken(loginUser);
        //log.info("用户数据已提前刷新");
    }

    @After("permissionCheck()")
    public void afterAdvice(JoinPoint joinPoint) throws IOException {

        // 获取request对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (StringUtils.isNull(attributes)) return;
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();

        // 获取登录用户信息
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (ObjectUtil.isEmpty(loginUser)){
            response.sendError(401,"登录过期，请重新登录。");
            log.warn("登录过期，请重新登录。");
            return;
        }
        TtUser user = userService.getById(loginUser.getUserId());

        // 设置更新后的属性
        UserData userData = loginUser.getUserData();
        // 对象拷贝
        BeanUtils.copyBeanProp(userData, user);
        // 获取上级用户
        TtUser ParentUser = userService.getById(user.getParentId());
        if (StringUtils.isNotNull(ParentUser))
            userData.setParentInvitationCode(ParentUser.getInvitationCode());    // 上级邀请码
        loginUser.setUserData(userData);

        // 刷新token
        tokenService.refreshToken(loginUser);
        //log.info("用户数据缓存刷新完毕");
    }
}
