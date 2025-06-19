package com.ruoyi.thirdparty.common.service.impl;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.thirdparty.common.model.ApiSmsBody;
import com.ruoyi.thirdparty.common.service.ApiSmsService;
import com.ruoyi.thirdparty.dxb.client.SMSBaoClient;
import com.ruoyi.thirdparty.dxb.config.SMSBaoConfig;
import com.ruoyi.thirdparty.note.service.YunXinNoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import oshi.driver.mac.net.NetStat;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ApiSmsServiceImpl implements ApiSmsService {

    private final RedisCache redisCache;
    private final TtUserService userService;
    private final YunXinNoteService yunXinNoteService;

    @Autowired
    private SMSBaoConfig smsBaoConfig;

    public ApiSmsServiceImpl(RedisCache redisCache,
                             TtUserService userService,
                             YunXinNoteService yunXinNoteService) {
        this.redisCache = redisCache;
        this.userService = userService;
        this.yunXinNoteService = yunXinNoteService;
    }

    @Override
    public String getVerifyCode(ApiSmsBody smsBody) {
        String phoneNumber = smsBody.getPhoneNumber();
        String type = smsBody.getType();
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY;
        if (StringUtils.isEmpty(type)) {
            return "类型不能为空";
        }
        if (StringUtils.isEmpty(phoneNumber)) {
            return "手机号不能为空";
        }
        if (!Validator.isMobile(phoneNumber)) {
            return "手机号格式错误，请检查手机号是否输入正确！";
        }
        if ("1".equals(type) && !userService.checkPhoneUnique(TtUser.builder().phoneNumber(phoneNumber).build())) {
            return "手机号'" + phoneNumber + "'已被注册！";
        }
        if ("2".equals(type) || "3".equals(type) || "4".equals(type)) {
            TtUser ttUser = new LambdaQueryChainWrapper<>(userService.getBaseMapper()).eq(TtUser::getPhoneNumber, phoneNumber).one();
            if (StringUtils.isNull(ttUser)) return "该手机号未在本站注册！";
        }
        if ("1".equals(type)) {
            verifyKey = verifyKey + "ApiRegister_" + phoneNumber;
        } else if ("2".equals(type)) {
            verifyKey = verifyKey + "ApiLogin_" + phoneNumber;
        } else if ("3".equals(type)) {
            verifyKey = verifyKey + "UpdatePhoneNumber_" + phoneNumber;
        } else if ("4".equals(type)) {
            verifyKey = verifyKey + "ApiForgetPassword_" + phoneNumber;
        }else if ("5".equals(type)) {
            phoneNumber = SecurityUtils.getLoginUser().getUsername().trim();
            verifyKey = verifyKey + "ApiUpdateStream_" + phoneNumber;
        }
        String cacheCode = redisCache.getCacheObject(verifyKey);
        if (StringUtils.isNotEmpty(cacheCode)) {
            return "验证码已发送，请注意手机短信信息！";
        }
        String randomCode = RandomUtil.randomNumbers(4);

        //是否开启短信包
        log.info("调用短信api===>");
        String code = "";
        if (smsBaoConfig.getEnable()){
            log.info("短信宝api，验证码【{}】===>", randomCode);
            code = new SMSBaoClient(smsBaoConfig).sendSMSVerCode(phoneNumber, randomCode);

        }else {
            log.info("云信api，验证码【{}】===>", randomCode);
            code = yunXinNoteService.sendNote(phoneNumber, randomCode);

        }

        if (StringUtils.isEmpty(code)) {
            return "发送短信验证码时出现异常！";
        }

        redisCache.setCacheObject(verifyKey, code, 5, TimeUnit.MINUTES);
        return "";
    }

    @Override
    public String validateCaptcha(String code, String verifyKey) {
        verifyKey = CacheConstants.CAPTCHA_CODE_KEY + verifyKey;
        String captcha = redisCache.getCacheObject(verifyKey);
        if (!code.equalsIgnoreCase(captcha)) return "验证码错误";
        redisCache.deleteObject(verifyKey);
        return "success";
    }
}
