package com.ruoyi.admin.util.core.fight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PrizePool {

    private String key;

    private String boxId;

    private String playerType;

    // 物品件数
    private Integer goodsNumber;

    // ornamentId : number
    private Map<String, Integer> boxSpace;

    // 减库存
    public PrizePool sub(String ornamentId) {
//        this.goodsNumber--;
//        this.boxSpace.put(ornamentId, this.boxSpace.get(ornamentId) - 1);
        return this;
    }

}
