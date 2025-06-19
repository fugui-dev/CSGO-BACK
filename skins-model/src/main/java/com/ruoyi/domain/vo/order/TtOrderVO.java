package com.ruoyi.domain.vo.order;

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
public class TtOrderVO implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    @Excel(name = "用户ID")
    private Integer userId;

    @Excel(name = "第三方")
    private String thirdParty;
    /**
     * 0卡密 1支付宝  2微信
     */
    @Excel(name = "订单类型")
    private String type;

    @Excel(name = "商品ID")
    private Integer goodsId;

    @Excel(name = "商品价格")
    private BigDecimal goodsPrice;

    @Excel(name = "商品数量")
    private Integer goodsNum;

    @Excel(name = "商品总价")
    private BigDecimal totalAmount;

    @Excel(name = "订单号")
    private String orderId;

    @Excel(name = "外部订单号")
    private String outTradeNo;

    @Excel(name = "sign验签密钥")
    private String sign;

    @Excel(name = "支付状态")
    private String status;

    @Excel(name = "支付跳转链接")
    private String payUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
