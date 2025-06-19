package com.ruoyi.domain.entity;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
@TableName(value = "tt_ornament")
public class TtOrnament implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Excel(name = "饰品唯一名称英文")
    @TableId(value = "market_hash_name", type = IdType.INPUT)
    private String marketHashName;

    @TableField(value = "id", fill = FieldFill.INSERT)
    private Long id;

    @Excel(name = "zbt_id")
    @TableField("zbt_id")
    private Long zbtId;

    @Excel(name = "yyyouping_id")
    @TableField("yyyouping_id")
    private Long yyyoupingId;

    @Excel(name = "长名称")
    @TableField("name")
    private String name;

    @Excel(name = "本网站使用价格(rmb)")
    private BigDecimal usePrice;

    @Excel(name = "图片")
    private String imageUrl;


    // TODO: 2024/4/5 加载饰品这里暂时这样写！！！！！！！
    // @Override
    // public boolean equals(Object o) {
    //     if (this == o) return true;
    //     if (o == null || getClass() != o.getClass()) return false;
    //     TtOrnament that = (TtOrnament) o;
    //     boolean equals = Objects.equals(this.marketHashName, that.marketHashName);
    //     // 如果已存在，补充信息
    //     if (equals) {
    //         this.yyyoupingId = ObjectUtil.isNotEmpty(that.yyyoupingId) && ObjectUtil.isEmpty(this.yyyoupingId) ? that.yyyoupingId : this.yyyoupingId;
    //         this.zbtId = ObjectUtil.isNotEmpty(that.zbtId) && ObjectUtil.isEmpty(this.zbtId) ? that.zbtId : this.zbtId;
    //         that.yyyoupingId = ObjectUtil.isNotEmpty(this.yyyoupingId) && ObjectUtil.isEmpty(that.yyyoupingId) ? this.yyyoupingId : that.yyyoupingId;
    //         that.zbtId = ObjectUtil.isNotEmpty(this.zbtId) && ObjectUtil.isEmpty(that.zbtId) ? this.zbtId : that.zbtId;
    //
    //         that.price = ObjectUtil.isNotEmpty(this.price) && ObjectUtil.isEmpty(that.price) ? this.price : that.price;
    //         that.quantity = ObjectUtil.isNotEmpty(this.quantity) && ObjectUtil.isEmpty(that.quantity) ? this.quantity : that.quantity;
    //         that.usePrice = ObjectUtil.isNotEmpty(this.usePrice) && ObjectUtil.isEmpty(that.usePrice) ? this.usePrice : that.usePrice;
    //
    //         that.shortName = ObjectUtil.isNotEmpty(this.shortName) ? this.shortName : that.shortName;
    //         that.imageUrl = ObjectUtil.isNotEmpty(this.imageUrl) ? this.imageUrl : that.imageUrl;
    //
    //         that.type = ObjectUtil.isNotEmpty(this.type) ? this.type : that.type;
    //         that.typeName = ObjectUtil.isNotEmpty(this.typeName) ? this.typeName : that.typeName;
    //
    //         that.quality = ObjectUtil.isNotEmpty(this.quality) && ObjectUtil.isEmpty(that.quality) ? this.quality : that.quality;
    //         that.qualityName = ObjectUtil.isNotEmpty(this.qualityName) && ObjectUtil.isEmpty(that.qualityName) ? this.qualityName : that.qualityName;
    //         that.qualityColor = ObjectUtil.isNotEmpty(this.qualityColor) && ObjectUtil.isEmpty(that.qualityColor) ? this.qualityColor : that.qualityColor;
    //
    //         that.rarity = ObjectUtil.isNotEmpty(this.rarity) && ObjectUtil.isEmpty(that.rarity) ? this.rarity : that.rarity;
    //         that.rarityName = ObjectUtil.isNotEmpty(this.rarityName) && ObjectUtil.isEmpty(that.rarityName) ? this.rarityName : that.rarityName;
    //         that.rarityColor = ObjectUtil.isNotEmpty(this.rarityColor) && ObjectUtil.isEmpty(that.rarityColor) ? this.rarityColor : that.rarityColor;
    //
    //         that.exterior = ObjectUtil.isNotEmpty(this.exterior) && ObjectUtil.isEmpty(that.exterior) ? this.exterior : that.exterior;
    //         that.exteriorName = ObjectUtil.isNotEmpty(this.exteriorName) && ObjectUtil.isEmpty(that.exteriorName) ? this.exteriorName : that.exteriorName;
    //
    //         that.updateTime = ObjectUtil.isNotEmpty(this.updateTime) && ObjectUtil.isEmpty(that.updateTime) ? this.updateTime : that.updateTime;
    //
    //     }
    //     return equals;
    // }

    @Override
    public int hashCode() {
        return Objects.hash(this.marketHashName);
    }

    @Excel(name = "在售最低价")
    private BigDecimal price;

    @Excel(name = "在售数量")
    private Integer quantity;

    @Excel(name = "短名称")
    private String shortName;

    @Excel(name = "类别")
    private String type;

    @Excel(name = "类别中文名")
    private String typeName;

    @Excel(name = "类别hash名")
    @TableField("type_hash_name")
    private String typeHashName;

    @Excel(name = "品质")
    private String quality;

    @Excel(name = "品质hash")
    private String qualityHashName;

    @Excel(name = "品质名称")
    private String qualityName;

    @Excel(name = "品质颜色")
    private String qualityColor;

    @Excel(name = "稀有度")
    private String rarity;

    private String rarityHashName;

    @Excel(name = "稀有度名称")
    private String rarityName;

    @Excel(name = "稀有度颜色")
    private String rarityColor;

    @Excel(name = "外观")
    private String exterior;

    @Excel(name = "外观")
    private String exteriorHashName;

    @Excel(name = "外观名称")
    private String exteriorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private String remark;

    // 0上架 1下架
    private String isPutaway;

    // 0是本网站自定义道具 1否
    private String isProprietaryProperty;

}
