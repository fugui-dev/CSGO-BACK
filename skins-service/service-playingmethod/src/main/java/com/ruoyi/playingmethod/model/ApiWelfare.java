package com.ruoyi.playingmethod.model;

import com.ruoyi.domain.other.TtBox;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApiWelfare {

    /**
     * 福利ID
     */
    private Integer welfareId;

    /**
     * 福利名称
     */
    private String welfareName;

    /**
     * 福利类型
     */
    private String type;

    /**
     * VIP等级
     */
    private Integer vipLevel;

    /**
     * 是否具备领取条件（0否 1是）
     */
    private String eligible;

    /**
     * 领取状态（0未领取 1已领取）
     */
    private String claimStatus;

    /**
     * 对应等级达标金额
     */
    private BigDecimal rechargeThreshold;

    /**
     * 宝箱信息
     */
    private TtBox ttBox;
}
