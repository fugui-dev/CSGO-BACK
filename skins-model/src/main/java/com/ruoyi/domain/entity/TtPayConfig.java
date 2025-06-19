package com.ruoyi.domain.entity;

import java.math.BigDecimal;

import lombok.Data;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 支付配置对象 tt_pay_config
 * 
 * @author ruoyi
 * @date 2024-06-25
 */

@Data
public class TtPayConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /**  */
    private Long id;

    /** 支付名称 */
    @Excel(name = "支付名称")
    private String payName;

    /** 支付方标识（不可随意改动） */
    @Excel(name = "支付方标识", readConverterExp = "不=可随意改动")
    private String payTag;

    /** 支付类型（1支付宝，2微信） */
    @Excel(name = "支付类型", readConverterExp = "1=支付宝，2微信")
    private Long payType;

    /** 最小充值金额 */
    @Excel(name = "最小充值金额")
    private BigDecimal payMinMoney;

    /** 最大充值金额 */
    @Excel(name = "最大充值金额")
    private BigDecimal payMaxMoney;

    /** 用户最小充值金额 */
    @Excel(name = "用户最小充值金额")
    private BigDecimal userTotalMinMoney;

    /** 排序 */
    @Excel(name = "排序")
    private Long sort;

    /** 状态（0启用，1禁用） */
    @Excel(name = "状态", readConverterExp = "0=启用，1禁用")
    private Long status;

}
