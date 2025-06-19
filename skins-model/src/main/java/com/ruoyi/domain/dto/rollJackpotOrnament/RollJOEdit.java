package com.ruoyi.domain.dto.rollJackpotOrnament;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.ruoyi.common.annotation.Excel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class RollJOEdit {

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

    @Excel(name = "饰品图片id")
    private String ornamentImgId;

    @Excel(name = "饰品图片")
    private String imgUrl;

    @Excel(name = "饰品名称")
    private String ornamentName;

    @Excel(name = "饰品级别ID")
    private Integer ornamentsLevelId;

    @Excel(name = "饰品数量")
    private Integer ornamentsNum;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

}
