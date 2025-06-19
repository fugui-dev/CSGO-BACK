package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.other.TtMessage;

import java.util.List;

public interface TtMessageService extends IService<TtMessage> {

    List<TtMessage> queryList(TtMessage ttMessage);
    String delByIds(List<Integer> ids);

    String singleMessage(Integer messageId, List<Integer> userIds);

    String massMessaging(Integer messageId);


}
