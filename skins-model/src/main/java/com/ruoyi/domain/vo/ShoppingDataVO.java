package com.ruoyi.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class ShoppingDataVO {
    private Integer id;
    private String itemName;
    private BigDecimal usePrice;
    private BigDecimal useCredits;
    private String imageUrl;
    private String marketHashName;
    private String itemId;
    private BigDecimal price;
    private String shortName;
    private String type;
    private String typeName;
    private String quality;
    private String qualityName;
    private String qualityColor;
    private String rarity;
    private String rarityName;
    private String rarityColor;
    private String exterior;
    private String exteriorName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    private String remark;
    private String isPutaway;
}
