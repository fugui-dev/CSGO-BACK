package com.ruoyi.domain.entity.roll;

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
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
@TableName(value = "tt_time_roll_user")
public class TtTimeRollUser implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    @Excel(name = "时间Roll房ID")
    private Integer timeRollId;

    @Excel(name = "用户ID")
    private Integer userId;

    @Excel(name = "奖池饰品列表ID")
    private Integer jackpotOrnamentsId;

    @Excel(name = "奖池饰品ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentsId;

    @Excel(name = "宝箱记录ID")
    private Long boxRecordId;

    private String status;

    @Excel(name = "指定者")
    private String designatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date joinTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @Excel(name = "结束状态", readConverterExp = "0=未开始,1=已结束")
    private String endStatus;
}
