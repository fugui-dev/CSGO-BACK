package com.ruoyi.thirdparty.wechat.mapper;

import com.ruoyi.thirdparty.wechat.entity.TianXinOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TianXinMapper {
    void insertTianXinOrder(TianXinOrder tianXinOrder);

    TianXinOrder selectTianXinOrderByOrderId(String sdorderno);

    void updateTianXinOrder(TianXinOrder tianXinOrder);

    void deleteDataTianXinOrder();

    void deleteDataTtCoinRechargeRecord();
}
