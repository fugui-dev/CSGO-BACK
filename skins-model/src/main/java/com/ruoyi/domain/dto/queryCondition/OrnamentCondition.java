package com.ruoyi.domain.dto.queryCondition;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class OrnamentCondition implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableField(value = "id", fill = FieldFill.INSERT)
    private Long id;

    @Excel(name = "长名称")
    @TableField("name")
    private String name;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    @Excel(name = "类别")
    private String type;

    @Excel(name = "品质")
    private String quality;

    @Excel(name = "稀有度")
    private String rarity;

    @Excel(name = "外观")
    private String exterior;

    // 1上架 0下架
    private String isPutaway;

    // 0是本网站自定义道具 1否
    private Boolean isProprietaryProperty;

    @Min(value = 1,message = "最小1")
    private Integer page;

    @Max(value = 20,message = "最大20")
    private Integer size;

    @Min(value = 1,message = "最小1")
    private Integer pageNum;

    @Max(value = 20,message = "最大20")
    private Integer pageSize;

    private Integer limit;

    public OrnamentCondition(BigDecimal minPrice, BigDecimal maxPrice) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
}
