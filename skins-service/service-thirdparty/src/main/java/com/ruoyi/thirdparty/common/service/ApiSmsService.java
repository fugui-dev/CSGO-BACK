package com.ruoyi.thirdparty.common.service;

import com.ruoyi.thirdparty.common.model.ApiSmsBody;

public interface ApiSmsService {

    String getVerifyCode(ApiSmsBody smsBody);

    String validateCaptcha(String code, String verifyKey);
}
