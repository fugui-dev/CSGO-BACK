package com.ruoyi.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.domain.other.TtRedPacketRecord;
import com.ruoyi.admin.mapper.TtRedPacketRecordMapper;
import com.ruoyi.admin.service.TtRedPacketRecordService;
import com.ruoyi.domain.other.TtRedPacketRecordBody;
import com.ruoyi.domain.vo.TtRedPacketRecordDataVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TtRedPacketRecordServiceImpl extends ServiceImpl<TtRedPacketRecordMapper, TtRedPacketRecord> implements TtRedPacketRecordService {

    @Override
    public List<TtRedPacketRecordDataVO> queryList(TtRedPacketRecordBody ttRedPacketRecordBody) {
        return baseMapper.queryList(ttRedPacketRecordBody);
    }
}
