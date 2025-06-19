package com.ruoyi.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
public class TtOrnamentVO implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId(value = "id",type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
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

    @Excel(name = "本网站使用价格")
    private BigDecimal usePrice;

    @Excel(name = "图片")
    private String imageUrl;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TtOrnamentVO that = (TtOrnamentVO) o;
        return Objects.equals(marketHashName, that.marketHashName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(marketHashName);
    }

    @Excel(name = "饰品唯一名称英文")
    private String marketHashName;

    // @Excel(name = "在售最低价")
    // private BigDecimal price;

    @Excel(name = "在售数量")
    private Integer quantity;

    @Excel(name = "短名称")
    private String shortName;

    @Excel(name = "类别")
    private String type;

    @Excel(name = "类别中文名")
    private String typeName;

    @Excel(name = "品质")
    private String quality;

    @Excel(name = "品质名称")
    private String qualityName;

    @Excel(name = "品质颜色")
    private String qualityColor;

    @Excel(name = "稀有度")
    private String rarity;

    @Excel(name = "稀有度名称")
    private String rarityName;

    @Excel(name = "稀有度颜色")
    private String rarityColor;

    @Excel(name = "外观")
    private String exterior;

    @Excel(name = "外观名称")
    private String exteriorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private String remark;

    private String isPutaway;

    private String isProprietaryProperty;
}
