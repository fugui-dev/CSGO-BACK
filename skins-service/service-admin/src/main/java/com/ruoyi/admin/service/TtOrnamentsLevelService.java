package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.other.TtOrnamentsLevel;

public interface TtOrnamentsLevelService extends IService<TtOrnamentsLevel> {

    String generateOrnamentsLevel(Integer num);

    String updateOrnamentsLevelById(TtOrnamentsLevel ttOrnamentsLevel);

    void truncateOrnamentsLevel();
}
