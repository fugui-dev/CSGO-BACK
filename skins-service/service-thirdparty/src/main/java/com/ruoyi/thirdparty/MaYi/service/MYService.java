package com.ruoyi.thirdparty.MaYi.service;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.dto.mayi.PayNotifyRequest;
import com.ruoyi.domain.entity.TtOrder;
import com.ruoyi.domain.entity.TtRechargeProd;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.CreateOrderParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface MYService {

    R ApiAddTrans(CreateOrderParam param, TtUser user, HttpServletRequest request);
    R ApiQueryTrans(Map<String,String> param, TtUser user);
    R ApiPropayTrans(Map<String,String> param, TtUser user);
    R ApiQueryBalancePHP(Map<String,String> param, TtUser user);

    public void payNotifyAccounting(TtOrder order, TtUser user, TtRechargeProd goods, Integer goodsNumber);

    String payNotify(PayNotifyRequest data);

    /**
     * 首充赠送
     */
    public Boolean firstChargeGiftAmount(TtUser ttUser, TtRechargeProd goods, Integer goodsNumber);

    /**
     * 推广等级充值赠送
     */
    public void promotionLevelChargeGiftAmount(TtUser ttUser, TtRechargeProd goods, Integer goodsNumber);


}
