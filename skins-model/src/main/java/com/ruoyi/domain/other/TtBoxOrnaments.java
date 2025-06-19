package com.ruoyi.domain.other;

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
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
@TableName(value = "tt_box_ornaments")
public class TtBoxOrnaments implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    @Excel(name = "宝箱ID")
    private Integer boxId;

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

    @Excel(name = "饰品级别")
    private Integer level;

    @Excel(name = "显示数量")
    private Integer odds;

    @Excel(name = "真实数量")
    private Integer realOdds;

    @Excel(name = "主播数量")
    private Integer anchorOdds;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private String remark;
}
