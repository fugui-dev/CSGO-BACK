package com.ruoyi.domain.vo.roll;

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
public class SimpleRollOrnamentVO implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    @Excel(name = "Roll房奖池ID")
    private Integer jackpotId;

    @Excel(name = "饰品ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentId;

    @Excel(name = "饰品价格")
    private BigDecimal price;

    @Excel(name = "饰品图片")
    private String imgUrl;

    // 类型
    private String typeName;

    // 外观
    private String exteriorName;

    @Excel(name = "饰品名称")
    private String ornamentName;

    @Excel(name = "饰品级别ID")
    private Integer ornamentsLevelId;

    @Excel(name = "饰品级别img")
    private String ornamentsLevelImg;

    @Excel(name = "饰品数量")
    private Integer ornamentsNum;

}
