package com.ruoyi.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.admin.mapper.TtBoxRecordsMapper;
import com.ruoyi.admin.service.TtBoxRecordsService;
import com.ruoyi.domain.other.TtBoxRecordsBody;
import com.ruoyi.domain.vo.TtBoxRecordsDataVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TtBoxRecordsServiceImpl extends ServiceImpl<TtBoxRecordsMapper, TtBoxRecords> implements TtBoxRecordsService {

    @Override
    public List<TtBoxRecordsDataVO> selectBoxRecordsList(TtBoxRecordsBody ttBoxRecordsBody) {
        return baseMapper.selectBoxRecordsList(ttBoxRecordsBody);
    }


}
