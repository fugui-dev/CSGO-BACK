package com.ruoyi.admin.service.impl;

import com.ruoyi.domain.other.ConfigData;
import com.ruoyi.admin.mapper.WebsiteSetupMapper;
import com.ruoyi.admin.service.ConfigService;
import com.ruoyi.admin.service.WebsiteSetupService;
import com.ruoyi.domain.other.OperationalStatistics;
import com.ruoyi.domain.other.ParameterSettingBody;
import com.ruoyi.system.service.ISysConfigService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WebsiteSetupServiceImpl implements WebsiteSetupService {

    private final ConfigService configService;
    private final ISysConfigService iSysConfigService;
    private final WebsiteSetupMapper websiteSetupMapper;

    public WebsiteSetupServiceImpl(ConfigService configService,
                                   ISysConfigService iSysConfigService,
                                   WebsiteSetupMapper websiteSetupMapper) {
        this.configService = configService;
        this.iSysConfigService = iSysConfigService;
        this.websiteSetupMapper = websiteSetupMapper;
    }

    @Override
    public List<OperationalStatistics> getOperationalStatistics() {
        return websiteSetupMapper.getOperationalStatistics();
    }

    @Override
    public ParameterSettingBody getParameterSetting() {
        List<ConfigData> parameterSettingList = websiteSetupMapper.selectParameterSettingList();
        Map<String, List<ConfigData>> parameterSettingMap = parameterSettingList.stream()
                .collect(Collectors.groupingBy(ConfigData::getConfigKey));
        ParameterSettingBody parameterSetting = ParameterSettingBody.builder().build();
        parameterSetting.setUsePricePremiumRate(parameterSettingMap.get("usePricePremiumRate").get(0).getConfigValue());
        parameterSetting.setExchangePriceRatio(parameterSettingMap.get("exchangePriceRatio").get(0).getConfigValue());
        parameterSetting.setRegisterRedPacket(parameterSettingMap.get("registerRedPacket").get(0).getConfigValue());
        parameterSetting.setMaxCompoundNum(parameterSettingMap.get("maxCompoundNum").get(0).getConfigValue());
        parameterSetting.setCompoundMinPrice(parameterSettingMap.get("compoundMinPrice").get(0).getConfigValue());
        parameterSetting.setCompoundMinPremiumRate(parameterSettingMap.get("compoundMinPremiumRate").get(0).getConfigValue());
        parameterSetting.setCompoundMaxPremiumRate(parameterSettingMap.get("compoundMaxPremiumRate").get(0).getConfigValue());
        parameterSetting.setZBTParities(parameterSettingMap.get("ZBTParities").get(0).getConfigValue());
        parameterSetting.setAutoDeliveryMinPrice(parameterSettingMap.get("autoDeliveryMinPrice").get(0).getConfigValue());
        parameterSetting.setBuyPricePremiumRate(parameterSettingMap.get("buyPricePremiumRate").get(0).getConfigValue());
        parameterSetting.setWebsiteMaintenance(parameterSettingMap.get("websiteMaintenance").get(0).getConfigValue());
        parameterSetting.setShoppingMaintenance(parameterSettingMap.get("shoppingMaintenance").get(0).getConfigValue());
        parameterSetting.setFightMaintenance(parameterSettingMap.get("fightMaintenance").get(0).getConfigValue());
        parameterSetting.setBindBoxMaintenance(parameterSettingMap.get("bindBoxMaintenance").get(0).getConfigValue());
        parameterSetting.setRollMaintenance(parameterSettingMap.get("rollMaintenance").get(0).getConfigValue());
        parameterSetting.setCompoundMaintenance(parameterSettingMap.get("compoundMaintenance").get(0).getConfigValue());
        return parameterSetting;
    }

    @Override
    public String updateParameterSetting(ParameterSettingBody parameterSettingBody) {
        String usePricePremiumRate = parameterSettingBody.getUsePricePremiumRate(),
                exchangePriceRatio = parameterSettingBody.getExchangePriceRatio(),
                registerRedPacket = parameterSettingBody.getRegisterRedPacket(),
                maxCompoundNum = parameterSettingBody.getMaxCompoundNum(),
                compoundMinPrice = parameterSettingBody.getCompoundMinPrice(),
                compoundMinPremiumRate = parameterSettingBody.getCompoundMinPremiumRate(),
                compoundMaxPremiumRate = parameterSettingBody.getCompoundMaxPremiumRate(),
                zbtParities = parameterSettingBody.getZBTParities(),
                autoDeliveryMinPrice = parameterSettingBody.getAutoDeliveryMinPrice(),
                buyPricePremiumRate = parameterSettingBody.getBuyPricePremiumRate(),
                websiteMaintenance = parameterSettingBody.getWebsiteMaintenance(),
                shoppingMaintenance = parameterSettingBody.getShoppingMaintenance(),
                fightMaintenance = parameterSettingBody.getFightMaintenance(),
                bindBoxMaintenance = parameterSettingBody.getBindBoxMaintenance(),
                rollMaintenance = parameterSettingBody.getRollMaintenance(),
                compoundMaintenance = parameterSettingBody.getCompoundMaintenance();
        List<ConfigData> parameterSettingList = websiteSetupMapper.selectParameterSettingList();
        Map<String, List<ConfigData>> parameterSettingMap = parameterSettingList.stream().collect(Collectors.groupingBy(ConfigData::getConfigKey));
        List<ConfigData> updateList = new ArrayList<>();
        ConfigData usePricePremiumRateSysConfig = parameterSettingMap.get("usePricePremiumRate").get(0);
        usePricePremiumRateSysConfig.setConfigValue(usePricePremiumRate);
        updateList.add(usePricePremiumRateSysConfig);
        ConfigData exchangePriceRatioSysConfig = parameterSettingMap.get("exchangePriceRatio").get(0);
        exchangePriceRatioSysConfig.setConfigValue(exchangePriceRatio);
        updateList.add(exchangePriceRatioSysConfig);
        ConfigData registerRedPacketSysConfig = parameterSettingMap.get("registerRedPacket").get(0);
        registerRedPacketSysConfig.setConfigValue(registerRedPacket);
        updateList.add(registerRedPacketSysConfig);
        ConfigData maxCompoundNumSysConfig = parameterSettingMap.get("maxCompoundNum").get(0);
        maxCompoundNumSysConfig.setConfigValue(maxCompoundNum);
        updateList.add(maxCompoundNumSysConfig);
        ConfigData compoundMinPriceSysConfig = parameterSettingMap.get("compoundMinPrice").get(0);
        compoundMinPriceSysConfig.setConfigValue(compoundMinPrice);
        updateList.add(compoundMinPriceSysConfig);
        ConfigData compoundMinPremiumRateSysConfig = parameterSettingMap.get("compoundMinPremiumRate").get(0);
        compoundMinPremiumRateSysConfig.setConfigValue(compoundMinPremiumRate);
        updateList.add(compoundMinPremiumRateSysConfig);
        ConfigData compoundMaxPremiumRateSysConfig = parameterSettingMap.get("compoundMaxPremiumRate").get(0);
        compoundMaxPremiumRateSysConfig.setConfigValue(compoundMaxPremiumRate);
        updateList.add(compoundMaxPremiumRateSysConfig);
        ConfigData zbtParitiesSysConfig = parameterSettingMap.get("ZBTParities").get(0);
        zbtParitiesSysConfig.setConfigValue(zbtParities);
        updateList.add(zbtParitiesSysConfig);
        ConfigData buyPricePremiumRateSysConfig = parameterSettingMap.get("buyPricePremiumRate").get(0);
        buyPricePremiumRateSysConfig.setConfigValue(buyPricePremiumRate);
        updateList.add(buyPricePremiumRateSysConfig);
        ConfigData autoDeliveryMinPriceSysConfig = parameterSettingMap.get("autoDeliveryMinPrice").get(0);
        autoDeliveryMinPriceSysConfig.setConfigValue(autoDeliveryMinPrice);
        updateList.add(autoDeliveryMinPriceSysConfig);
        ConfigData websiteMaintenanceSysConfig = parameterSettingMap.get("websiteMaintenance").get(0);
        websiteMaintenanceSysConfig.setConfigValue(websiteMaintenance);
        updateList.add(websiteMaintenanceSysConfig);
        ConfigData shoppingMaintenanceSysConfig = parameterSettingMap.get("shoppingMaintenance").get(0);
        shoppingMaintenanceSysConfig.setConfigValue(shoppingMaintenance);
        updateList.add(shoppingMaintenanceSysConfig);
        ConfigData fightMaintenanceSysConfig = parameterSettingMap.get("fightMaintenance").get(0);
        fightMaintenanceSysConfig.setConfigValue(fightMaintenance);
        updateList.add(fightMaintenanceSysConfig);
        ConfigData bindBoxMaintenanceSysConfig = parameterSettingMap.get("bindBoxMaintenance").get(0);
        bindBoxMaintenanceSysConfig.setConfigValue(bindBoxMaintenance);
        updateList.add(bindBoxMaintenanceSysConfig);
        ConfigData rollMaintenanceSysConfig = parameterSettingMap.get("rollMaintenance").get(0);
        rollMaintenanceSysConfig.setConfigValue(rollMaintenance);
        updateList.add(rollMaintenanceSysConfig);
        ConfigData compoundMaintenanceSysConfig = parameterSettingMap.get("compoundMaintenance").get(0);
        compoundMaintenanceSysConfig.setConfigValue(compoundMaintenance);
        updateList.add(compoundMaintenanceSysConfig);
        for (ConfigData configData : updateList) {
            configService.updateById(configData);
        }
        iSysConfigService.resetConfigCache();
        return "";
    }
}
