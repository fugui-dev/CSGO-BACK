package com.ruoyi.domain.entity.roll;

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
@TableName(value = "tt_roll")
public class TtRoll implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Excel(name = "ROLL房ID")
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;

    @Excel(name = "奖池ID")
    private Integer jackpotId;

    // 创建者
    private Integer userId;

    // 0官方 1主播
    private String rollType;

    @Excel(name = "Roll房名称")
    private String rollName;

    @Excel(name = "Roll房描述")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @Excel(name = "人数限制")
    private Integer peopleNum;

    @Excel(name = "Roll房参与密码")
    private String rollPassword;

    @Excel(name = "充值门槛")
    private BigDecimal minRecharge;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date rechargeStartTime;

    @Excel(name = "排序依据")
    private Integer sortBy;

    // 0未开奖 1已开奖
    @Excel(name = "状态")
    private String rollStatus;

    @Excel(name = "创建者")
    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @TableField(select = false)
    private String delFlag;

}
