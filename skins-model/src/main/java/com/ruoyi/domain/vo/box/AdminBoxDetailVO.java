package com.ruoyi.domain.vo.box;

import com.ruoyi.domain.vo.TtBoxOrnamentsDataVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminBoxDetailVO {

    // 统计信息
    // 宝箱完全开启所需金额
    private BigDecimal amountConsumed;

    // 宝箱内饰品总价值
    private BigDecimal aggregateAmount;

    // 利润
    private BigDecimal profit;

    // 利润率
    private BigDecimal profitMargin;

    // 宝箱内物品明细
    private List<TtBoxOrnamentsDataVO> ornamentsDetail;

}
