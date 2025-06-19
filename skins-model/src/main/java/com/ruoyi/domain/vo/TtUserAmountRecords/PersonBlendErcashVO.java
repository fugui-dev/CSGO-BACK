package com.ruoyi.domain.vo.TtUserAmountRecords;

// 个人综合收支统计

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PersonBlendErcashVO {

    private Integer userId;

    private String userName;

    // 金币
    private BigDecimal amount;
    // 弹药
    private BigDecimal credits;
    // 合计
    private BigDecimal total;

}
