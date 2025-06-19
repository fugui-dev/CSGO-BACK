package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.dto.userRecord.AmountRecordsDetailCondition;
import com.ruoyi.domain.vo.TtUserAccountRecordsRankVO;
import com.ruoyi.domain.entity.recorde.TtUserAmountRecords;
import com.ruoyi.domain.other.TtUserAmountRecordsBody;
import com.ruoyi.domain.vo.TtUserAmountRecords.PWelfareVO;
import com.ruoyi.domain.vo.TtUserAmountRecords.UserAmountDetailVO;

import java.sql.Timestamp;
import java.util.List;

public interface TtUserAmountRecordsService extends IService<TtUserAmountRecords> {

    List queryList(TtUserAmountRecordsBody ttUserAmountRecordsBody);

    Page<TtUserAccountRecordsRankVO> rank(Timestamp begin, Timestamp end, Integer page, Integer size);

    R pWelfareRecords(Integer userId, Integer page, Integer size);

    List<UserAmountDetailVO> userAccountDetail(AmountRecordsDetailCondition param);

    R<PWelfareVO> pCommissionRecords(Integer intValue, Integer page, Integer size);

}
