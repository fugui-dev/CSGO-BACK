package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.other.TtVipLevel;

public interface TtVipLevelService extends IService<TtVipLevel> {

    String generateVipLevel(Integer num);

    String updateVipLevelById(TtVipLevel ttVipLevel);
    void truncateVipLevel();
}
