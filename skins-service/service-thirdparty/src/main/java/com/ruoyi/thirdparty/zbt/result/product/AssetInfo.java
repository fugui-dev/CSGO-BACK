package com.ruoyi.thirdparty.zbt.result.product;

import lombok.Data;

import java.util.List;

@Data
public class AssetInfo {

    private String assetId;
    private String classId;
    private String classInfoId;
    private String ext;
    private String fadeColor;
    private String fraudwarning;
    private List<Gems> gems;
    private String gradient;
    private String inspectImageUrl;

    private String instanceId;

    private String lastStyle;
    private String levelColor;
    private String levelName;
    private Integer paintIndex;
    private Integer paintSeed;
    private List<ItemAssetStickerDTO> stickers;
    private String styleId;
    private String styleProgress;
    private List<ItemAssetClassInfoStyleDTO> styles;
    private String wear;

}
