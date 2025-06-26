package com.ruoyi.domain.other;

import com.baomidou.mybatisplus.annotation.IdType;
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
@TableName(value = "tt_recharge_record")
public class TtRechargeRecord implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Excel(name = "充值记录ID")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Excel(name = "用户ID")
    @TableField("user_id")
    private Integer userId;

    @Excel(name = "上级用户ID")
    private Integer parentId;

    @Excel(name = "到账金额")
    private BigDecimal arrivalAmount;

    @Excel(name = "实际支付金额")
    private BigDecimal amountActuallyPaid;

    @Excel(name = "最终金额")
    private BigDecimal finallyPrice;

    @Excel(name = "订单号")
    @TableField("order_id")
    private String orderId;

    @Excel(name = "外部订单号")
    private String outTradeNo;

    private String status;

    private String channelType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @TableField(select = false)
    private String delFlag;

    @Excel(name = "百度营销推广渠道id")
    private Long bdPromotionChannelId;

    @Excel(name = "是否主播虚拟充值（0=否,1=是）")
    private Integer anchorVirtual;
}
