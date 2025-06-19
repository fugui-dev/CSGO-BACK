package com.ruoyi.domain.other;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.domain.common.constant.UserType;
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
@TableName(value = "tt_upgrade_record")
public class TtUpgradeRecord implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Excel(name = "升级记录ID")
    @TableId
    private Long id;

    @Excel(name = "用户ID")
    private Integer userId;

    @Excel(name = "昵称")
    private String nickName;

    @Excel(name = "用户类型")
    private String userType;

    @Excel(name = "消耗金额")
    private BigDecimal amountConsumed;

    @Excel(name = "可升级饰品ID")
    @TableField("target_upgrade_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetUpgradeId;

    @Excel(name = "目标饰品ID")
    @TableField("target_ornament_id")
    private Long targetOrnamentId;

    @Excel(name = "目标饰品价格")
    @TableField("target_ornament_price")
    private BigDecimal targetOrnamentPrice;

    @Excel(name = "概率")
    @TableField("probability")
    private Integer probability;

    @Excel(name = "最终奖励饰品集合")
    @TableField("gain_ornament_list")
    private String gainOrnamentList;

    @Excel(name = "最终奖励总价")
    @TableField("gain_ornaments_price")
    private BigDecimal gainOrnamentsPrice;

    @Excel(name = "成败")
    @TableField("is_victory")
    private Boolean isVictory;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date openTime;

    @TableField(exist = false)
    private String userAvatar;
}
