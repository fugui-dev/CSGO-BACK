package com.ruoyi.thirdparty.wechat.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.thirdparty.wechat.entity.TtUserLsjl;
import com.ruoyi.thirdparty.wechat.mapper.TtUserLsjlMapper;
import com.ruoyi.thirdparty.wechat.service.ITtUserLsjlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 流水记录Service业务层处理
 * 
 * @author junhai
 * @date 2023-08-19
 */
@Service
public class TtUserLsjlServiceImpl implements ITtUserLsjlService
{
    @Autowired
    private TtUserLsjlMapper ttUserLsjlMapper;

    /**
     * 新增流水记录
     * 
     * @param ttUserLsjl 流水记录
     * @return 结果
     */
    @Override
    public int insertTtUserLsjl(TtUserLsjl ttUserLsjl)
    {
        ttUserLsjl.setCreateTime(DateUtils.getNowDate());
        return ttUserLsjlMapper.insertTtUserLsjl(ttUserLsjl);
    }

}
