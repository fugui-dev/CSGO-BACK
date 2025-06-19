package com.ruoyi.thirdparty.zbt.service;

import com.ruoyi.thirdparty.zbt.param.*;
import com.ruoyi.thirdparty.zbt.result.ResultZbt;
import com.ruoyi.thirdparty.zbt.result.buy.OpenBuyResultDTO;
import com.ruoyi.thirdparty.zbt.result.order.OrderBuyDetailDTO;
import com.ruoyi.thirdparty.zbt.result.product.AvailableMarketList;
import com.ruoyi.thirdparty.zbt.result.product.OrnPriceInfo;
import com.ruoyi.thirdparty.zbt.result.product.ProductFilters;
import com.ruoyi.thirdparty.zbt.result.product.SearchData;
import com.ruoyi.thirdparty.zbt.result.user.BusinessData;
import com.ruoyi.thirdparty.zbt.result.user.GenericCheckSteamOutPut;
import com.ruoyi.thirdparty.zbt.result.user.UserAccountModel;

import java.util.List;

public interface ZBTService {

    ResultZbt<UserAccountModel> balance();

    ResultZbt<GenericCheckSteamOutPut> userSteamInfo(UserSteamInfoParams userSteamInfoParams);

    public ResultZbt<CreateCheckSteam> createSteamCheck(CreateCheckSteam params);

    ResultZbt<BusinessData> developmentInfo();

    ResultZbt<SearchData> search(SearchParams params);

    ResultZbt<ProductFilters> productFilters();

    ResultZbt<AvailableMarketList> productList(ProductListParams productListParams);

    ResultZbt<OpenBuyResultDTO> tradeBuy(NormalBuyParamV2DTO normalBuyParamV2DTO);

    ResultZbt<OpenBuyResultDTO> tradeQuickBuy(QuickBuyParamV2DTO quickBuyParamV2DTO);

    ResultZbt<OrderBuyDetailDTO> orderBuyDetail(OrderBuyDetailParams orderBuyDetailParams);

    //批量查询饰品价格
    ResultZbt<List<OrnPriceInfo>> queryPriceBatch(SearchPriceBatchParams searchPriceBatchParams);

}
