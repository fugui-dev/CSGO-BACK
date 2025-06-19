package com.ruoyi.domain.other;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
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
@TableName(value = "tt_red_packet")
public class TtRedPacket implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    @Excel(name = "红包标题")
    private String title;

    @Excel(name = "红包描述")
    private String description;

    @Excel(name = "发放个数")
    private Integer num;

    @Excel(name = "单个红包金额区间")
    private String amount;

    @Excel(name = "用户ID")
    private Integer userId;

    @Excel(name = "红包口令")
    private String password;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date openingTime;

    // 有效期限
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date validity;

    // 0,正在使用 1,结束
    private Integer status;

    @Excel(name = "创建者")
    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    //0启用 1禁用
    @TableField("use_status")
    private Integer useStatus;

    @TableField(select = false)
    @TableLogic(value = "0",delval = "1")
    private Integer delFlag;
}
