package com.ruoyi.user.service;

import com.ruoyi.common.core.domain.AjaxResult;

public interface ApiLoginService {

    String login(String username, String password);

    AjaxResult verificationCodeLogin(String phoneNumber, String code);
}
