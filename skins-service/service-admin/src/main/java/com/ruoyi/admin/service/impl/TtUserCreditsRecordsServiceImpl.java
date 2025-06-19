package com.ruoyi.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.admin.mapper.TtUserMapper;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.entity.recorde.TtUserCreditsRecords;
import com.ruoyi.admin.mapper.TtUserCreditsRecordsMapper;
import com.ruoyi.admin.service.TtUserCreditsRecordsService;
import com.ruoyi.domain.other.TtUserCreditsRecordsBody;
import com.ruoyi.domain.vo.TtUserCreditsRecordsRankVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

@Service
public class TtUserCreditsRecordsServiceImpl extends ServiceImpl<TtUserCreditsRecordsMapper, TtUserCreditsRecords> implements TtUserCreditsRecordsService {


    @Autowired
    private TtUserMapper userMapper;

    @Override
    public List<TtUserCreditsRecords> queryList(TtUserCreditsRecordsBody ttUserCreditsRecordsBody) {
        return baseMapper.queryList(ttUserCreditsRecordsBody);
    }

    /**
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<TtUserCreditsRecordsRankVO> rank(Timestamp begin,Timestamp end,Integer page, Integer size) {

        Page<TtUserCreditsRecordsRankVO> pageInfo = new Page<>(page,size);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String beginT = dateFormat.format(begin);
        String endT = dateFormat.format(end);

        List<TtUserCreditsRecordsRankVO> rank = baseMapper.rank(page - 1, size,beginT ,endT);
        pageInfo.setRecords(rank);
        pageInfo.setTotal(rank.size());
        return pageInfo;
    }

    @Override
    public Page<TtUserCreditsRecords> pWelfareRecords(Integer uid, Integer type, Integer page, Integer size) {

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Timestamp today = new Timestamp(c.getTimeInMillis());

        Page<TtUserCreditsRecords> pageInfo = new Page<>(page, size);
        pageInfo.setOptimizeCountSql(false);

        LambdaQueryWrapper<TtUserCreditsRecords> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(TtUserCreditsRecords::getUserId,uid)
                .eq(TtUserCreditsRecords::getSource, TtAccountRecordSource.P_WELFARE.getCode());
        if (type.equals(1)){
            wrapper.ge(TtUserCreditsRecords::getCreateTime,today);
        }else if (type.equals(2)){
            c.add(Calendar.DAY_OF_MONTH,-1);
            Timestamp yesterday = new Timestamp(c.getTimeInMillis());
            wrapper
                    .ge(TtUserCreditsRecords::getCreateTime,yesterday)
                    .le(TtUserCreditsRecords::getCreateTime,today);
        }else if (type.equals(3)){
            c.add(Calendar.DAY_OF_MONTH,-6);
            Timestamp week = new Timestamp(c.getTimeInMillis());
            wrapper
                    .ge(TtUserCreditsRecords::getCreateTime,week);
        }

        return page(pageInfo,wrapper);
    }
}
