package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.domain.other.TtRedPacket;
import com.ruoyi.domain.other.TtRedPacketBody;
import com.ruoyi.domain.vo.TtRedPacketDataVO;

import java.util.List;

public interface TtRedPacketService extends IService<TtRedPacket> {

    List<TtRedPacketDataVO> queryList(TtRedPacketBody ttRedPacketBody);

    List<String> insertRedPacket(TtRedPacketDataVO ttRedPacketDataVO);
    AjaxResult updateRedPacketById(TtRedPacket redPacket);
}
