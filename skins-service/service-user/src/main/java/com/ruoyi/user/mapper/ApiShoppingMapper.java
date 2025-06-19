package com.ruoyi.user.mapper;

import com.ruoyi.domain.other.ApiShoppingBody;
import com.ruoyi.domain.vo.ApiShoppingDataVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApiShoppingMapper {
    List<ApiShoppingDataVO> list(@Param("shoppingBody") ApiShoppingBody shoppingBody, @Param("exchangePriceRatio") Integer exchangePriceRatio);
}
