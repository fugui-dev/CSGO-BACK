package com.ruoyi.thirdparty.zbt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "zbt")
public class ZBTProperties {
    private String appKey;
    private String language;
    private String appId;
    private String baseUrl;
    private String userBalance = "/open/user/v1/t-coin/balance";
    private String userCreate = "/open/user/steam-check/create";
    private String userSteamInfo = "/open/user/steam-info";
    private String developmentSave = "/open/development/callback-url/save";
    private String developmentInfo = "/open/development/info";
    private String userSteamAccounts = "/open/user/steam-accounts";
    private String productSearch = "/open/product/v2/search";
    private String productInfo = "/open/product/price/info";
    private String productFilters = "/open/product/v1/filters";
    private String productList = "/open/product/v1/sell/list";
    private String product = "/open/product/v1/sell/product";
    private String productV1Info = "/open/product/v1/info";
    private String tradeBuy = "/open/trade/v2/buy";
    private String tradeQuickBuy = "/open/trade/v2/quick-buy";
    private String orderBuyerList = "/open/order/buyer/list";
    private String orderSellerList = "/open/order/seller/list";
    private String orderBuyDetail = "/open/order/v2/buy/detail";
    private String orderSellerDetail = "/open/order/v2/seller/detail";
    private String orderBuyerCancel = "/open/order/buyer-cancel";
    private String orderSellerCancel = "/open/order/seller-cancel";
    private String offerStatus = "/open/offer/v1/status";
    private String offerSendAssets = "/open/offer/v1/send-assets";
    private String v1Inventory = "/open/v1/inventory/{steamId}/{appId}/";
    private String v1Bind = "/open/v1/seller/steam/bind/";
    private String v1Items = "/open/v1/sale/items";
    private String v1Deliver = "/open/v1/sale/deliver";
    private String v1SellList = "/open/v1/sell-list";
    private String v1ModifyPrice = "/open/v1/modify-price";
    private String v1OffSale = "/open/v1/off-sale";
}
