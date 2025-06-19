package com.ruoyi.domain.vo;

import com.ruoyi.common.annotation.Excel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
//@Accessors(chain = true)
@Builder
public class TtUserAccountRecordsRankVO {

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

    @Excel(name = "变动金额")
    private BigDecimal account;

//    @Excel(name = "最终弹药")
//    private BigDecimal finalCredits;

//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private Date createTime;

    private String remark;

    private Integer accountRank;
}
