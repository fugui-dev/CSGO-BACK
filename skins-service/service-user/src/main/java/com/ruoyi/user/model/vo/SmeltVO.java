package com.ruoyi.user.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SmeltVO {

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("饰品ID")
    @TableField("ornament_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentId;

    @ApiModelProperty("饰品名称")
    private String ornamentName;

    @ApiModelProperty("饰品价格")
    private BigDecimal ornamentsPrice;

    @ApiModelProperty("饰品图片")
    private String imageUrl;

    @ApiModelProperty("饰品级别ID")
    private Integer ornamentsLevelId;

    @ApiModelProperty("饰品级别图片")
    private String ornamentLevelImg;

    @ApiModelProperty("持有者_用户ID")
    private Integer holderUserId;


}
