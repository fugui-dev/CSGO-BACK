package com.ruoyi.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class TtRollPrizeDataVO {

    // roll玩家id
    @NotNull(message = "必须指定获奖人。")
    private Integer rollUserId;

    /**
     * 获奖用户ID
     */
    private Integer userId;

    // 奖池物品id
    @NotNull(message = "必须指定奖品id。")
    private Integer rollJackpotOrnamentId;

    // 数量
    @Min(value = 1,message = "数量最少为1")
    private Integer ornamentNum;

    // roll房id
    private Integer rollId;

    /**
     * 指定人
     */
    private String designatedBy;

    /**
     * 获奖用户昵称
     */
    private String nickName;
    private String imageUrl;
    private Integer jackpotOrnamentsListId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentsId;
    private Integer ornamentsLevelId;
    private String itemName;
    private String shortName;
    private BigDecimal usePrice;
    private Integer rollUserPrizeId;

}
