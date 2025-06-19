package com.ruoyi.user.service;

import com.ruoyi.domain.other.TtBanner;
import com.ruoyi.domain.vo.ApiContentDataVO;

import java.util.List;

public interface ApiWebsiteSetupService {

    List<TtBanner> getBannerList();

    ApiContentDataVO getContentByType(String alias);
}
