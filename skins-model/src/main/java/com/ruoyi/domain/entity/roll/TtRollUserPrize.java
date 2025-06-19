package com.ruoyi.domain.entity.roll;

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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
@TableName(value = "tt_roll_user_prize")
public class TtRollUserPrize implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    @Excel(name = "roll用户ID")
    @TableField("roll_user_id")
    private Integer rollUserId;

    @Excel(name = "Roll房奖池id")
    @TableField("roll_jackpot_id")
    private Integer rollJackpotId;

    @Excel(name = "奖池饰品ID")
    @TableField("roll_jackpot_ornament_id")
    private Integer rollJackpotOrnamentId;

    @Excel(name = "饰品ID")
    @TableField("ornament_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentId;

    @Excel(name = "饰品名称")
    @TableField("ornament_name")
    private String ornamentName;

    @Excel(name = "饰品图片")
    @TableField("img_url")
    private String imgUrl;

    @Excel(name = "饰品价格")
    @TableField("price")
    private BigDecimal price;

    @Excel(name = "奖品数量")
    @TableField("number")
    private Integer number;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
