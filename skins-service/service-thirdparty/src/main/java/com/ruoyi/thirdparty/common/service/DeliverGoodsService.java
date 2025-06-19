package com.ruoyi.thirdparty.common.service;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.dto.deliver.TradeBuyParam;
import com.ruoyi.domain.vo.AvailableMarketOrnamentVO;
import com.ruoyi.thirdparty.common.controller.DeliverGoodsController.GetAvailableMarketListParam;
import com.ruoyi.thirdparty.zbt.param.ProductListParams;

import java.util.List;

public interface DeliverGoodsService {

    R getAvailableMarketList(Long ornamentsId, Integer partyType);

    R tradeBuy(TradeBuyParam param);

    String synchronousStatus(String outTradeNo);

    void autoDelivery(Integer userId);

    List<AvailableMarketOrnamentVO> getAvailableMarketListByHashName(String marketHashName, ProductListParams productListParams);

    R getAvailableMarketList(GetAvailableMarketListParam param);

}
