package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.TtBonus;

import java.math.BigDecimal;

public interface TtBonusService extends IService<TtBonus> {

    String updateBonusById(TtBonus ttBonus);

    void bonus(Integer userId, Integer rechargeRecordId);

    boolean insertParentUserPromotionRecord(Integer userId, TtUser parentUser, BigDecimal amountActuallyPaid, Integer rechargeRecordId);

    void updateParentUserPromotionLevel(TtUser parentUser);
}
