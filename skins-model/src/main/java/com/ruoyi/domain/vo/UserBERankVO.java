package com.ruoyi.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
public class UserBERankVO implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Excel(name = "id")
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    @Excel(name = "user_id")
    @TableField("user_id")
    private Integer userId;

    @TableField("nick_name")
    private String nickName;

    // 头像
    private String avatar;

    @Excel(name = "amount")
    @TableField("amount")
    private BigDecimal amount;

    @Excel(name = "credits")
    @TableField("credits")
    private BigDecimal credits;

    @Excel(name = "total")
    @TableField("total")
    private BigDecimal total;

    // @Excel(name = "source")
    // @TableField("source")
    // private Integer source;

    @Excel(name = "create_time")
    @TableField("create_time")
    private Timestamp createTime;

    @TableField("be_rank")
    private Integer beRank;

    // @Excel(name = "update_time")
    // @TableField("update_time")
    // private Timestamp updateTime;

}
