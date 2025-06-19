package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.other.TtRechargeCard;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface TtRechargeCardService extends IService<TtRechargeCard> {

    List<TtRechargeCard> queryList(TtRechargeCard ttRechargeCard);

    List<String> generateCard(Integer rechargeListId, Integer num);

    void export(HttpServletResponse response, TtRechargeCard ttRechargeCard);
}
