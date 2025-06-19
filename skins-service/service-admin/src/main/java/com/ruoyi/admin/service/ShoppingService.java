package com.ruoyi.admin.service;

import com.ruoyi.domain.other.ShoppingBody;
import com.ruoyi.domain.vo.ShoppingDataVO;
import com.ruoyi.common.core.page.PageDataInfo;

public interface ShoppingService {
    PageDataInfo<ShoppingDataVO> list(ShoppingBody shoppingBody);
}
