package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.entity.roll.TtRollJackpot;

public interface TtRollJackpotService extends IService<TtRollJackpot> {

    String removeRollJackpotById(Long jackpotId);
}
