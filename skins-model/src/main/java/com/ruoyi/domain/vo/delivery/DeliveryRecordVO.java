package com.ruoyi.domain.vo.delivery;

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
public class DeliveryRecordVO implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    @Excel(name = "用户ID")
    private Integer userId;

    @Excel(name = "用户背包记录ID")
    private Long boxRecordsId;

    @Excel(name = "饰品ID")
    @TableField("ornament_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentId;

    @Excel(name = "饰品图片")
    private String ornamentName;

    @Excel(name = "饰品图片")
    private String ornamentImg;

    @Excel(name = "饰品图片")
    private String ornamentLevelImg;

    @Excel(name = "市场唯一hashName")
    @TableField("market_hash_name")
    private String marketHashName;

    @Excel(name = "饰品价格")
    private BigDecimal ornamentsPrice;

    @Excel(name = "网站订单号")
    private String outTradeNo;

    //网站发货模式（1人工发货 2自动发货 3主播号提取）
    @Excel(name = "网站发货模式")
    private Integer delivery;

    @Excel(name = "实际购买价格")
    private BigDecimal buyPrice;

    @Excel(name = "第三方平台订单号")
    private String orderId;

    @Excel(name = "第三方发货模式")
    private Integer thirdpartyDelivery;

    // DeliveryOrderStatus 发货订单状态（0发起提货 1待发货 3待收货 10订单完成 11订单取消）
    @Excel(name = "发货订单状态")
    private Integer status;
    private String message;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
