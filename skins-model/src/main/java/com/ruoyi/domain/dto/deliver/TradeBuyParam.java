package com.ruoyi.domain.dto.deliver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TradeBuyParam {

    // 提货记录id
    private Integer deliveryRecordId;

    // 提货平台
    private Integer PartyType;

    // 平台的物品在售记录id
    private Long productId;

}
