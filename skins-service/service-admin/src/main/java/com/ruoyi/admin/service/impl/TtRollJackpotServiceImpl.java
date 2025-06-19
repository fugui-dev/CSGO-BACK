package com.ruoyi.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.domain.entity.roll.TtRollJackpot;
import com.ruoyi.admin.mapper.TtRollJackpotMapper;
import com.ruoyi.admin.service.TtRollJackpotService;
import org.springframework.stereotype.Service;

@Service
public class TtRollJackpotServiceImpl extends ServiceImpl<TtRollJackpotMapper, TtRollJackpot> implements TtRollJackpotService {

    @Override
    public String removeRollJackpotById(Long jackpotId) {
        this.removeById(jackpotId);
        return "";
    }
}
