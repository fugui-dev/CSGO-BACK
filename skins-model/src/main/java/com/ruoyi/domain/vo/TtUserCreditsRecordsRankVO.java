package com.ruoyi.domain.vo;

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
//@Accessors(chain = true)
@Builder
public class TtUserCreditsRecordsRankVO {

//    @TableId
//    private Integer id;

    @Excel(name = "用户ID")
    private Integer userId;

    private String userName;

    private String nickName;

    private String avatar;

//    private String type;
//
//    private String source;

    @Excel(name = "变动弹药")
    private BigDecimal credits;

//    @Excel(name = "最终弹药")
//    private BigDecimal finalCredits;

//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private Date createTime;

    private String remark;

    private Integer creditsRank;
}
