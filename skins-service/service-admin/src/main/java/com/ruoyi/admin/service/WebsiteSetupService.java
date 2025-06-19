package com.ruoyi.admin.service;

import com.ruoyi.domain.other.OperationalStatistics;
import com.ruoyi.domain.other.ParameterSettingBody;

import java.util.List;

public interface WebsiteSetupService {

    List<OperationalStatistics> getOperationalStatistics();
    ParameterSettingBody getParameterSetting();

    String updateParameterSetting(ParameterSettingBody parameterSettingBody);

}
