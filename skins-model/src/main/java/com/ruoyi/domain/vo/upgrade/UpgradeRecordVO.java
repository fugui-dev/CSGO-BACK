package com.ruoyi.domain.vo.upgrade;

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

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class UpgradeRecordVO implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Excel(name = "升级记录ID")
    @TableId
    private Long id;

    @Excel(name = "用户ID")
    private Integer userId;

    @Excel(name = "昵称")
    private String nickName;

    private String avatar;

    @Excel(name = "用户类型")
    private String userType;

    @Excel(name = "消耗金额")
    private BigDecimal amountConsumed;

    private BigDecimal profit;

    @Excel(name = "可升级饰品ID")
    @TableField("target_upgrade_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetUpgradeId;

    @Excel(name = "目标饰品ID")
    @TableField("target_ornament_id")
    private Long targetOrnamentId;

    private String targetOrnamentName;

    private String targetOrnamentImg;

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

    // 记录总条数 分页辅助
    private Integer total;

    @NotNull(message = "页码不能为空")
    private Integer page;

    @NotNull(message = "分页长度不能为空")
    private Integer size;
}
