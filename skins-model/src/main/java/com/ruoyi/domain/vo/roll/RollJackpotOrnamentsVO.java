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
public class RollJackpotOrnamentsVO implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    @Excel(name = "Roll房奖池ID")
    private Integer jackpotId;

    @Excel(name = "Roll房奖池饰品ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentsId;

    @Excel(name = "饰品价格")
    private BigDecimal price;

    @Excel(name = "饰品图片")
    private String imgUrl;

    @Excel(name = "饰品名称")
    private String ornamentName;

    @Excel(name = "饰品级别ID")
    private Integer ornamentLevelId;

    private String ornamentLevelImg;

    @Excel(name = "饰品数量")
    private Integer ornamentsNum;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
