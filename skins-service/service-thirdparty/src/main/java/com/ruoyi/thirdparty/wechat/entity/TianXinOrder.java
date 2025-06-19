package com.ruoyi.thirdparty.wechat.entity;

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
public class TianXinOrder {
    private String orderId;    // 系统订单号(唯一)
    private String payType;    // 支付方式:3支付宝H5,4支付宝App
    private String goodsId;    // 商户商品ID
    private BigDecimal goodsPrice; // 商户商品价格
    private Integer goodsNum;  // 商户商品数量
    private BigDecimal totalAmount;    // 付款总价
    private String userIp; // 用户IP
    private Integer payStatus; // 支付状态: 0待支付，1已支付，2,取消支付
    private Date createTime;   // 创建时间
    private Date updateTime;   // 状态更新时间
    private Long userId;   // 订单发起用户ID
    private String userName;   // 订单发起用户名
    private String remark;  // 备注
    private String callBackOrderId;  // 回传单号
    private String callBackMsg;   // 回传信息
    private String callBackStatus;    // 回传状态
    private String sign;    // sign
    private String subject; // 商品名
}
