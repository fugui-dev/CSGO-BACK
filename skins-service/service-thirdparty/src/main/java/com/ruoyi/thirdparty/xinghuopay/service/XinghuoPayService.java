package com.ruoyi.thirdparty.xinghuopay.service;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.CreateOrderParam;

import java.util.Map;

public interface XinghuoPayService {

    R pay(CreateOrderParam param, TtUser ttUser, String ip);

    String notify(Map<String, String> params);
}
