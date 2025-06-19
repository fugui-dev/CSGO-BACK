package com.ruoyi.thirdparty.wechat.service;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.thirdparty.wechat.domain.PayOrderParam;

import javax.servlet.http.HttpServletRequest;

public interface TianXinService {
    AjaxResult createOrder(PayOrderParam param, TtUser user, String ip);

    String callBack(HttpServletRequest request);

    AjaxResult queryOrderStatus(String sdorderno);
}
