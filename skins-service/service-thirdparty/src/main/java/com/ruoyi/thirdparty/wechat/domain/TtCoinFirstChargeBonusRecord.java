package com.ruoyi.thirdparty.wechat.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TtCoinFirstChargeBonusRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long uid;   // 用户ID
    private Long fcbid; // 首充奖励表ID
    private BigDecimal rewards; // 奖励价值
    private Integer status; // 冻结状态(默认1) 0解冻 1冻结 2销毁
    private Date createTime;   // 创建时间
    private Date latestUseTime; // 最晚使用时间
    private Date useTime;  // 使用时间
    private String remark;  // 备注
}
