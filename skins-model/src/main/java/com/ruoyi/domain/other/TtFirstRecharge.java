package com.ruoyi.domain.other;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 首充赠送对象 tt_first_recharge
 *
 * @author ruoyi
 * @date 2024-06-21
 */
public class TtFirstRecharge extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Integer id;

    /** 充值金额下限 */
    @Excel(name = "充值金额下限")
    private BigDecimal minAmount;

    /** 赠送比例 */
    @Excel(name = "赠送比例")
    private BigDecimal ratio;

    /** 规则描述 */
    @Excel(name = "规则描述")
    private String description;

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getId()
    {
        return id;
    }
    public void setMinAmount(BigDecimal minAmount)
    {
        this.minAmount = minAmount;
    }

    public BigDecimal getMinAmount()
    {
        return minAmount;
    }
    public void setRatio(BigDecimal ratio)
    {
        this.ratio = ratio;
    }

    public BigDecimal getRatio()
    {
        return ratio;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("minAmount", getMinAmount())
                .append("ratio", getRatio())
                .append("description", getDescription())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
