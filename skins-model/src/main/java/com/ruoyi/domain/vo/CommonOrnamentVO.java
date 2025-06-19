package com.ruoyi.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

/*
通用饰品信息vo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class CommonOrnamentVO implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    private Integer yyId;

    // 是表里的itemId（ZBT官网饰品id）
    private Integer zbtId;

    // 也是zbt的长名称
    @Excel(name = "名称")
    @TableField("name")
    private String name;

    @Excel(name = "饰品唯一名称英文")
    private String marketHashName;

    @Excel(name = "类型")
    private Integer type;

    @Excel(name = "type_name")
    @TableField("type_name")
    private String typeName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private Timestamp updateTime;

    @Excel(name = "本网站使用价格")
    private BigDecimal usePrice;

    @Excel(name = "图片")
    private String imageUrl;

    @Excel(name = "在售数量")
    private Integer quantity;




    // yy
    @Excel(name = "type_hash_name")
    @TableField("type_hash_name")
    private String typeHashName;

    @Excel(name = "weapon_id")
    @TableField("weapon_id")
    private Integer weaponId;

    @Excel(name = "weapon_name")
    @TableField("weapon_name")
    private String weaponName;

    @Excel(name = "weapon_hash_name")
    @TableField("weapon_hash_name")
    private String weaponHashName;

    @Excel(name = "在售最低价")
    private BigDecimal price;

    @Excel(name = "短名称")
    private String shortName;

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

    private String remark;

    private String isPutaway;

    private String isProprietaryProperty;
}
