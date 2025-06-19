package com.ruoyi.domain.entity;

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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
@TableName(value = "tt_box_records")
public class TtBoxRecords implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @Excel(name = "用户ID")
    private Integer userId;

    @Excel(name = "宝箱ID")
    private Integer boxId;

    @Excel(name = "宝箱名称")
    private String boxName;

    @Excel(name = "宝箱价格")
    private BigDecimal boxPrice;

    @Excel(name = "饰品ID")
    @TableField("ornament_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentId;

    @Excel(name = "市场唯一hash名称")
    @TableField("market_hash_name")
    private String marketHashName;

    @Excel(name = "zbt ID")
    @TableField("ornaments_zbt_id")
    private String ornamentsZbtId;

    @Excel(name = "yy ID")
    @TableField("ornaments_yy_id")
    private String ornamentsYyId;

    @Excel(name = "饰品名称")
    private String ornamentName;

    @Excel(name = "饰品价格")
    private BigDecimal ornamentsPrice;

    @Excel(name = "饰品图片")
    private String imageUrl;

    @Excel(name = "饰品级别ID")
    private Integer ornamentsLevelId;

    @Excel(name = "饰品级别图片")
    private String ornamentLevelImg;

    // 0、在背包显示 1、已分解
    @Excel(name = "状态")
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    // 0、对战
    @Excel(name = "饰品来源")
    private Integer source;

    @Excel(name = "对战ID")
    private Integer fightId;

    @Excel(name = "对战模式：回合数")
    private Integer fightRoundNumber;

    @Excel(name = "Roll房ID")
    private Integer rollId;

    @Excel(name = "持有者_用户ID")
    private Integer holderUserId;


    //是否赠品
    private Boolean isOpenBox2Gift = false;

    // 用于对战模式，游戏完全结束才显示
    // @TableField("is_show")
    // private Integer isShow;
}
