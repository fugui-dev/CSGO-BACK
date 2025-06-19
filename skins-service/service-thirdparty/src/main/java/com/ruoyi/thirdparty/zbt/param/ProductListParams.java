package com.ruoyi.thirdparty.zbt.param;

import com.ruoyi.common.annotation.Excel;
import lombok.Builder;
import lombok.Data;
// zbt平台接口查询参数
@Data
@Builder
public class ProductListParams {

    // // 第三方平台
    // private Integer partyType;
    //
    // // 饰品在该平台上的id
    // private String id;

    private String itemId;
    private String appKey;
    private String language;
    private String containSticker;
    private String delivery;
    private String endWear;
    private String gemEnName;
    private String gemId;
    private String gemIds;
    private String levelIds;
    private String levelName;
    private String limit;
    private String orderBy;
    private String page;
    private String slotStickerIds;
    private String sort;
    private String startWear;
    private String stickerCapsule;
    private String stickerIds;
    private String styleId;
    private String hasFraudwarning;
    private String standardPrice;
    private String withHistory;
    @Excel(name = "饰品唯一名称英文")
    private String marketHashName;
}
