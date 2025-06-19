package com.ruoyi.thirdparty.zyZFB.service;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.other.CreateOrderParam;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.thirdparty.zyZFB.controller.ZyController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface zyService {

    R ApiAddTrans(CreateOrderParam param, TtUser user, HttpServletRequest request);
    R ApiQueryTrans(Map<String,String> param, TtUser user);
    R ApiPropayTrans(Map<String,String> param, TtUser user);
    R ApiQueryBalancePHP(Map<String,String> param, TtUser user);

    String payNotify(ZyController.PayNotifyData data);
}
