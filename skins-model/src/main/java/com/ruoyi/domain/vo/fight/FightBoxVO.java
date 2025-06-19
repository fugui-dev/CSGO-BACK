package com.ruoyi.domain.vo.fight;

import com.ruoyi.domain.vo.TtBoxOrnamentsDataVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FightBoxVO implements Serializable{

    private static final long serialVersionUID = 1L;

    private Integer boxId;
    private Integer number;
    private String boxImg01;
    private String boxImg02;

    private BigDecimal boxPrice;

    // 宝箱所有饰品信息
    private List<TtBoxOrnamentsDataVO> ornaments;

    public FightBoxVO(Integer boxId,Integer number,String boxImg01,String boxImg02, BigDecimal boxPrice){
        this.boxId = boxId;
        this.number = number;
        this.boxImg01 = boxImg01;
        this.boxImg02 = boxImg02;
        this.boxPrice = boxPrice;
    }
    //
    // public FightBoxVO(String number,String boxImg01,String boxImg02){
    //     this.number = Integer.valueOf(number);
    //     this.boxImg01 = boxImg01;
    //     this.boxImg02 = boxImg02;
    // }

    // public FightBoxVO(String json){
    //
    //     ObjectMapper objectMapper = new ObjectMapper();
    //     try {
    //         FightBoxVO fightBoxVO = objectMapper.readValue(json, FightBoxVO.class);
    //         this.number = fightBoxVO.getNumber();
    //         this.boxImg01 = fightBoxVO.getBoxImg01();
    //         this.boxImg02 = fightBoxVO.getBoxImg02();
    //     } catch (JsonProcessingException e) {
    //         System.out.println("FightBoxVO【构造器异常】");
    //     }
    // }
}
