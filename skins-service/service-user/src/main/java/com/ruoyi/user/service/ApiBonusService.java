package com.ruoyi.user.service;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.entity.sys.TtUser;

import java.math.BigDecimal;

public interface ApiBonusService {

    R receiveRedPacket(String code, TtUser ttUser);
}
