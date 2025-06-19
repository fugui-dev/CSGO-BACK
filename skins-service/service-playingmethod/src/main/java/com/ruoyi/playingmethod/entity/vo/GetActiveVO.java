package com.ruoyi.playingmethod.entity.vo;

import com.ruoyi.admin.service.TtBonusReceiveRecordService;
import com.ruoyi.domain.entity.TtOrnament;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetActiveVO {

    @ApiModelProperty("福利名称(one实名福利金币，two实名福利弹药，three实名福利饰品)")
    private String name;

    @ApiModelProperty("福利金额")
    private BigDecimal awardMoney;


    @ApiModelProperty("是否已领取 0否，1是")
    private Integer getStatus;


    @ApiModelProperty("福利饰品")
    private TtOrnament ornament;

}
