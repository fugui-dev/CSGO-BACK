package com.ruoyi.domain.vo;

import com.ruoyi.domain.other.BoxDataBodyA;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FightRoomDataVO {

    List<BoxDataBodyA> boxDataList;
    private Integer fightId;
    private String fightStatus;
    private Integer boxNumTotal;
    private BigDecimal boxPriceTotal;
    private Integer userId;
    private String userName;
    private String nickName;
    private String avatar;
}
