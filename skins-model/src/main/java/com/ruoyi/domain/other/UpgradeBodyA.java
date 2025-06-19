package com.ruoyi.domain.other;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class UpgradeBodyA {

    // 饰品id
    //@NotNull(message = "饰品id不能为空")
    // private Long ornamentId;

    // 幸运升级饰品id
    @NotNull(message = "幸运升级饰品id不能为空")
    @ApiModelProperty("要升级的目标饰品ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long upgradeOrnamentId;

    @ApiModelProperty("要投入的背包内的物品ID，背包记录的ID不是饰品ID。仅限幸运升级2玩法中有效")
    private Integer packageOrnamentId;

    // 消费
    @NotNull(message = "消费金额不能为空")
    @ApiModelProperty("消费金额")
    private BigDecimal price;

    // 概率，增量
    @Range(min = 1,max = 100)
    @ApiModelProperty("幸运比例")
    private Integer probability;

    // private Integer id;
}
