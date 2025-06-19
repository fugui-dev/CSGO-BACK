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
@TableName(value = "tt_bonus_receive_record")
public class TtBonusReceiveRecord implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    @Excel(name = "充值福利福利类型，对应的是判断类型condition_type")
    private String type;

    @Excel(name = "福利ID")
    private Integer bonusId;

    @Excel(name = "VIP等级ID")
    private Integer vipLevelId;

    @Excel(name = "推广等级ID")
    private Integer promotionLevelId;

    @Excel(name = "领取人ID")
    private Integer userId;

    @Excel(name = "奖励类型")
    private String awardType;

    @Excel(name = "奖励ID", readConverterExp = "0=金币")
    private Integer awardId;

    @Excel(name = "奖励价值")
    private BigDecimal awardPrice;

    @Excel(name = "状态", readConverterExp = "0=未领取,1=已领取")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "领取时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date receiveTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
