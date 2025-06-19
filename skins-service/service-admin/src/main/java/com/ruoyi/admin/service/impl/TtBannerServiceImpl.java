package com.ruoyi.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.domain.other.TtBanner;
import com.ruoyi.admin.mapper.TtBannerMapper;
import com.ruoyi.admin.service.TtBannerService;
import com.ruoyi.common.config.RuoYiConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TtBannerServiceImpl extends ServiceImpl<TtBannerMapper, TtBanner> implements TtBannerService {

    @Override
    public String updateBannerById(TtBanner ttBanner) {
        String picture = ttBanner.getPicture();
        ttBanner.setPicture(RuoYiConfig.getDomainName() + picture);
        if (this.updateById(ttBanner)) return "";
        return "修改banner数据异常！";
    }
}
