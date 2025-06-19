package com.ruoyi.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.domain.other.TtOrnamentsLevel;
import com.ruoyi.admin.mapper.TtOrnamentsLevelMapper;
import com.ruoyi.admin.mapper.WebsiteSetupMapper;
import com.ruoyi.admin.service.TtOrnamentsLevelService;
import com.ruoyi.common.config.RuoYiConfig;
import org.springframework.stereotype.Service;


@Service
public class TtOrnamentsLevelServiceImpl extends ServiceImpl<TtOrnamentsLevelMapper, TtOrnamentsLevel> implements TtOrnamentsLevelService {

    private final WebsiteSetupMapper websiteSetupMapper;

    public TtOrnamentsLevelServiceImpl(WebsiteSetupMapper websiteSetupMapper) {
        this.websiteSetupMapper = websiteSetupMapper;
    }

    @Override
    public String generateOrnamentsLevel(Integer num) {
        for (int i = 1; i <= num; i++) {
            TtOrnamentsLevel ttOrnamentsLevel = TtOrnamentsLevel.builder().build();
            ttOrnamentsLevel.setLevel("");
            ttOrnamentsLevel.setLevelImg("");
            this.save(ttOrnamentsLevel);
        }
        return "";
    }

    @Override
    public String updateOrnamentsLevelById(TtOrnamentsLevel ttOrnamentsLevel) {
        String levelImg = ttOrnamentsLevel.getLevelImg();
        ttOrnamentsLevel.setLevelImg(RuoYiConfig.getDomainName() + levelImg);
        this.updateById(ttOrnamentsLevel);
        return "";
    }

    @Override
    public void truncateOrnamentsLevel() {
        websiteSetupMapper.truncateTtOrnamentsLevel();
    }
}
