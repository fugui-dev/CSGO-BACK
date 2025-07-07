package com.ruoyi.domain.vo;

import com.ruoyi.domain.entity.fight.FightSeat;
import com.ruoyi.domain.vo.fight.FightBoxVO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ApiFightListDataVO {

    private Integer id;

    private String model;

    private List<FightSeat> seats;

    private Integer roundNumber;

    private Integer playerNum;

    private String boxData;

    private Map<String, FightBoxVO> boxDataMap;

    private BigDecimal boxPriceTotal;

    private String status;

    private String userData;

    private String updateTime;

    private List<Integer> winnerIds;

    private BigDecimal openTotalPrice;


}
