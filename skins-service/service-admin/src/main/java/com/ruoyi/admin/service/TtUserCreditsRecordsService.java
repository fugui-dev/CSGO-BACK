package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.entity.recorde.TtUserCreditsRecords;
import com.ruoyi.domain.other.TtUserCreditsRecordsBody;
import com.ruoyi.domain.vo.TtUserCreditsRecordsRankVO;

import java.sql.Timestamp;
import java.util.List;

public interface TtUserCreditsRecordsService extends IService<TtUserCreditsRecords> {

    List<TtUserCreditsRecords> queryList(TtUserCreditsRecordsBody ttUserCreditsRecordsBody);

    Page<TtUserCreditsRecordsRankVO> rank(Timestamp begin,Timestamp end, Integer page, Integer size);

    Page<TtUserCreditsRecords> pWelfareRecords(Integer uid, Integer type, Integer page, Integer size);
}
