package com.ruoyi.thirdparty.alipay.service;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.CreateOrderParam;

import java.util.Map;

public interface AliPayService {

    R pay(CreateOrderParam param, TtUser ttUser, String ip);

    String callBack(Map<String, String> params);
}
