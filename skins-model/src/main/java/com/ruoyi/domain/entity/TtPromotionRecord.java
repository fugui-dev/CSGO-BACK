package com.ruoyi.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
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
@TableName(value = "tt_promotion_record")
public class TtPromotionRecord implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    @Excel(name = "用户ID")
    private Integer userId;

    @Excel(name = "下级用户ID")
    private Integer subordinateUserId;

    @Excel(name = "下级用户充值金额")
    private BigDecimal rechargePrice;

    /**
     * 下级用户总消费
     */
    @TableField(exist = false)
    private BigDecimal totalConsumption;

    @Excel(name = "返佣金额")
    private BigDecimal rebate;

    @Excel(name = "充值记录ID")
    private Integer rechargeRecordId;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
