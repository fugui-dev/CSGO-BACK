package com.ruoyi.thirdparty.common.service;

import com.ruoyi.domain.entity.sys.TtUser;

public interface ApiRechargeService {

    String cardPay(String password, TtUser ttUser);
}
