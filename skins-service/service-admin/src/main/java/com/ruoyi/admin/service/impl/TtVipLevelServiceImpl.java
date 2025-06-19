package com.ruoyi.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.domain.other.TtVipLevel;
import com.ruoyi.admin.mapper.TtVipLevelMapper;
import com.ruoyi.admin.mapper.WebsiteSetupMapper;
import com.ruoyi.admin.service.TtVipLevelService;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TtVipLevelServiceImpl extends ServiceImpl<TtVipLevelMapper, TtVipLevel> implements TtVipLevelService {

    private final WebsiteSetupMapper websiteSetupMapper;

    public TtVipLevelServiceImpl(WebsiteSetupMapper websiteSetupMapper) {
        this.websiteSetupMapper = websiteSetupMapper;
    }

    @Override
    public String generateVipLevel(Integer num) {
        for (int i = 1; i <= num; i++) {
            TtVipLevel ttVipLevel = TtVipLevel.builder().build();
            ttVipLevel.setName("VIP" + i);
            ttVipLevel.setIcon("");
            ttVipLevel.setRechargeThreshold(BigDecimal.ZERO);
            ttVipLevel.setCommissions(BigDecimal.ZERO);
            ttVipLevel.setAddedBonus(BigDecimal.ZERO);
            ttVipLevel.setCreateTime(DateUtils.getNowDate());
            this.save(ttVipLevel);
        }
        return "";
    }

    @Override
    public String updateVipLevelById(TtVipLevel ttVipLevel) {
        ttVipLevel.setIcon(RuoYiConfig.getDomainName() + ttVipLevel.getIcon());
        this.updateById(ttVipLevel);
        return "";
    }

    @Override
    public void truncateVipLevel() {
        websiteSetupMapper.truncateTtVipLevel();
    }
}
