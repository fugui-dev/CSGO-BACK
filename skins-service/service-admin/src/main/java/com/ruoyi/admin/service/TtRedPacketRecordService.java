package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.other.TtRedPacketRecord;
import com.ruoyi.domain.other.TtRedPacketRecordBody;
import com.ruoyi.domain.vo.TtRedPacketRecordDataVO;

import java.util.List;

public interface TtRedPacketRecordService extends IService<TtRedPacketRecord> {

    List<TtRedPacketRecordDataVO> queryList(TtRedPacketRecordBody ttRedPacketRecordBody);
}
