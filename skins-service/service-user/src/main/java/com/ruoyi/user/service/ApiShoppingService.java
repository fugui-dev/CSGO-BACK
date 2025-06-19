package com.ruoyi.user.service;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.ApiShoppingBody;
import com.ruoyi.domain.vo.ApiShoppingDataVO;

import java.math.BigDecimal;
import java.util.List;

public interface ApiShoppingService {

    List<ApiShoppingDataVO> list(ApiShoppingBody shoppingBody);

    R exchange(TtUser ttUser, Long ornamentsId);

    String integratingConversion(TtUser ttUser, BigDecimal credits);
}
