package com.ruoyi.domain.vo.TtUserAmountRecords;

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
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class TtUserBlendErcashVO implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Excel(name = "id")
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    @Excel(name = "user_id")
    @TableField("user_id")
    private Integer userId;

    @Excel(name = "金币")
    @TableField("amount")
    private BigDecimal amount;

    @Excel(name = "final_amount")
    @TableField("final_amount")
    private BigDecimal finalAmount;

    @Excel(name = "弹药")
    @TableField("credits")
    private BigDecimal credits;

    @Excel(name = "final_credits")
    @TableField("final_credits")
    private BigDecimal finalCredits;

    @Excel(name = "合计")
    @TableField("total")
    private BigDecimal total;

    @Excel(name = "来源")
    @TableField("source")
    private Integer source;

    @Excel(name = "收支类型")
    @TableField("type")
    private Integer type;

    @Excel(name = "create_time")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;

    @Excel(name = "update_time")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;

    @Excel(name = "笔记")
    @TableField("remark")
    private String remark;

}
