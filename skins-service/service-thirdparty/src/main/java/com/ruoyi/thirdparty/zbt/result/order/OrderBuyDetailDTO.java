package com.ruoyi.thirdparty.zbt.result.order;

import com.ruoyi.domain.common.constant.DeliveryOrderStatus;
import com.ruoyi.thirdparty.zbt.result.product.AssetInfo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderBuyDetailDTO {

    private AssetInfo assetInfo;
    private String createTime;
    private String deliverType;
    private String failedDesc;
    private OfferInfoDTO offerInfoDTO;
    private OpenItemInfo openItemInfo;
    private String orderId;
    private BigDecimal price;
    private String productId;
    private String receiveSteamId;

    //订单状态
    private DeliveryOrderStatus status;
    private String statusName;

}
