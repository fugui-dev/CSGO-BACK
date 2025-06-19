package com.ruoyi.thirdparty.zbt.result.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AvailableMarket {

    private Integer acceptBargain;
    private Integer appId;
    private String appName;
    private AssetInfo assetInfo;
    private String classInfoId;
    private BigDecimal cnyPrice;
    private Integer compensateType;
    private Integer delivery;
    private String description;
    private String id;
    private String imageUrl;
    private String inspect3dUrl;
    private Integer inspect3dViewable;
    private String inspectImageUrl;
    private String inspectUrl;
    private Integer inspectViewable;
    private Integer inspectable;

    private Integer isCollection;

    private String itemId;
    private ItemInfo itemInfo;
    private String itemName;
    private String marketHashName;
    private BigDecimal price;
    private SellerUserInfoVO sellerInfo;
    private BigDecimal sellerPrice;
    private Integer systemTime;

    private String token;
}
