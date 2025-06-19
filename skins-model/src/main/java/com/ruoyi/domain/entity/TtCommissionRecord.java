package com.ruoyi.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("tt_commission_record")
public class TtCommissionRecord extends Model<TtCommissionRecord> {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 推广人员ID
     */
    private Integer userId;

    /**
     * 佣金
     */
    private BigDecimal commission;

    /**
     * 汇总时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date summaryTime;

    /**
     * 领取状态（0未领取 1已领取）
     */
    private String claimStatus;

    /**
     * 领取时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date claimTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date create_time;

    /**
     * 总流水
     */
    private BigDecimal totalAmount;

    /**
     * 佣金率
     */
    private BigDecimal commissionRate;

    /**
     * 操作情况
     */
    private String operationBy;

}
