package com.ruoyi.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.annotation.Excel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
@TableName(value = "tt_recharge_prod")
public class TtRechargeProd implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    // 名称
    private String name;

    @Excel(name = "充值金额")
    private BigDecimal price;

    // 0启用，1禁用
    @Excel(name = "状态")
    private String status;

    @Excel(name = "卡密链接")
    private String cardLink;

    // 产品：金币
    @TableField("product_a")
    private BigDecimal productA;

    // 产品：弹药
    @TableField("product_c")
    private BigDecimal productC;
}
