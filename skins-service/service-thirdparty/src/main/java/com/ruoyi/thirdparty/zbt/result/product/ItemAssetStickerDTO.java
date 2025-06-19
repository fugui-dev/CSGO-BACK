package com.ruoyi.thirdparty.zbt.result.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemAssetStickerDTO {

    private String id;
    private String image;
    private String itemId;
    private String name;
    private BigDecimal price;
    private String slot;
    private String stickerId;
    private String type;
    private String wear;
}
