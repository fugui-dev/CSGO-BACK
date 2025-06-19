package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.other.TtBanner;

public interface TtBannerService extends IService<TtBanner> {
    String updateBannerById(TtBanner ttBanner);
}
