package com.ruoyi.domain.vo.TtUserAmountRecords;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.domain.entity.TtUserBlendErcash;
import com.ruoyi.domain.entity.recorde.TtUserAmountRecords;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class PWelfareVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 明细
    @ApiModelProperty("明细")
    private Page<TtUserBlendErcash> details;

    // 今日预计收益
    @ApiModelProperty("今日预计收益")
    private BigDecimal todayPredict;

    // 历史总收益
    @ApiModelProperty("历史总收益")
    private BigDecimal historyTotal;

    // 时间区间收益
    @ApiModelProperty("时间区间收益")
    private BigDecimal timeTotal;
}
