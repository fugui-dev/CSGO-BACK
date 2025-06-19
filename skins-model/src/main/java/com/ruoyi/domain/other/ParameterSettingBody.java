package com.ruoyi.domain.other;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class ParameterSettingBody {

    private String usePricePremiumRate;
    private String exchangePriceRatio;
    private String registerRedPacket;
    private String maxCompoundNum;
    private String compoundMinPrice;
    private String compoundMinPremiumRate;
    private String compoundMaxPremiumRate;
    private String ZBTParities;
    private String buyPricePremiumRate;
    private String autoDeliveryMinPrice;
    private String websiteMaintenance;
    private String shoppingMaintenance;
    private String fightMaintenance;
    private String bindBoxMaintenance;
    private String rollMaintenance;
    private String compoundMaintenance;
}
