package com.ruoyi.web.controller.log;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;

@Aspect
@Component
@Slf4j
public class GlobalLogAspect {
    @Pointcut("execution(public * com.ruoyi.*.controller..*.*(..))")
    public void requestAspect(){}
    @Before(value = "requestAspect()")
    public void methodBefore(JoinPoint joinPoint){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        Date currentTime = new Date();

        try {
            LoginUser loginUser = SecurityUtils.getLoginUser();

            //打印请求内容
            if (loginUser.getUserId() == null){
                //用户端日志
                log.info("{}请求地址：{}，请求类方法参数：{},访问用户:{}" , currentTime,
                        request.getRequestURL().toString(),Arrays.toString(joinPoint.getArgs()), loginUser.getUserId());

            }else {
                //管理端日志
                log.info("{}请求地址：{},请求方式：{}，请求类方法：{}，请求类方法参数：{},用户:{}" ,
                        currentTime,
                        request.getRequestURL().toString(),request.getMethod(),
                        joinPoint.getSignature(), Arrays.toString(joinPoint.getArgs()), loginUser.getUserId());

            }
        }catch (Exception e){
            log.warn("匿名访问==>请求地址：{}，请求类方法参数：{}", request.getRequestURL().toString(),Arrays.toString(joinPoint.getArgs()));
        }


    }
    //在方法执行完结后打印返回内容
    @AfterReturning(returning = "o",pointcut = "requestAspect()")
    public void methodAfterReturing(Object o ){
        log.info("返回数据:{}" , JSONObject.toJSONString(o));
    }
}