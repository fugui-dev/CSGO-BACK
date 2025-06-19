package com.ruoyi.thirdparty.wechat.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TtCoinFirstChargeBonus implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long cid;   // 充值商品ID
    private BigDecimal keyCoin;    // 充值金额
    private BigDecimal rewards; // 奖励价值
    private Long oid;   // 代金券饰品ID
    private Integer num;    // 奖励数量(默认5)
    private Integer status; // 开启状态(默认1) 1开启 0关闭
    private String name;
    private String img;
}
