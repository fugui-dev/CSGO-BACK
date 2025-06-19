package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.other.TtBoxRecordsBody;
import com.ruoyi.domain.vo.TtBoxRecordsDataVO;

import java.util.List;

public interface TtBoxRecordsService extends IService<TtBoxRecords> {

    List<TtBoxRecordsDataVO> selectBoxRecordsList(TtBoxRecordsBody ttBoxRecordsBody);


}
