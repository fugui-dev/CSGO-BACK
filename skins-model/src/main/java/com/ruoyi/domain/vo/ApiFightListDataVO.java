package com.ruoyi.domain.vo;

import com.ruoyi.domain.entity.fight.FightSeat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ApiFightListDataVO {

    private Integer id;

    private String model;

    private List<FightSeat> seats;

    private Integer roundNumber;

    private Integer playerNum;

    private String boxData;

    private BigDecimal boxPriceTotal;

    private String status;

    private String userData;

    private String updateTime;

    private List<Integer> winnerIds;

    private BigDecimal openTotalPrice;


}
