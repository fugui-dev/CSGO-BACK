package com.ruoyi.domain.other;

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
@TableName(value = "tt_recharge_card")
public class TtRechargeCard implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    @Excel(name = "充值列表ID")
    private Integer rechargeListId;

    @Excel(name = "金额")
    private BigDecimal price;

    @Excel(name = "卡密")
    private String password;

    @Excel(name = "状态")
    private String status;

    @Excel(name = "使用者ID")
    private Integer useUserId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date useTime;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @TableField(select = false)
    @Excel(name = "删除标志")
    private String delFlag;
}
