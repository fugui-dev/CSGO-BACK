package com.ruoyi.admin.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.admin.mapper.TtWelfareMapper;
import com.ruoyi.domain.other.TtWelfare;
import com.ruoyi.admin.service.TtWelfareService;

/**
 * 福利列表Service业务层处理
 *
 * @author ruoyi
 * @date 2024-05-11
 */
@Service
public class TtWelfareServiceImpl implements TtWelfareService
{
    @Autowired
    private TtWelfareMapper ttWelfareMapper;

    /**
     * 查询福利列表
     *
     * @param welfareId 福利列表主键
     * @return 福利列表
     */
    @Override
    public TtWelfare selectTtWelfareByWelfareId(Integer welfareId)
    {
        return ttWelfareMapper.selectTtWelfareByWelfareId(welfareId);
    }

    /**
     * 查询福利列表列表
     *
     * @param ttWelfare 福利列表
     * @return 福利列表
     */
    @Override
    public List<TtWelfare> selectTtWelfareList(TtWelfare ttWelfare)
    {
        return ttWelfareMapper.selectTtWelfareList(ttWelfare);
    }

    /**
     * 新增福利列表
     *
     * @param ttWelfare 福利列表
     * @return 结果
     */
    @Override
    public int insertTtWelfare(TtWelfare ttWelfare)
    {
        ttWelfare.setCreateTime(DateUtils.getNowDate());
        return ttWelfareMapper.insertTtWelfare(ttWelfare);
    }

    /**
     * 修改福利列表
     *
     * @param ttWelfare 福利列表
     * @return 结果
     */
    @Override
    public int updateTtWelfare(TtWelfare ttWelfare)
    {
        ttWelfare.setUpdateTime(DateUtils.getNowDate());
        return ttWelfareMapper.updateTtWelfare(ttWelfare);
    }

    /**
     * 批量删除福利列表
     *
     * @param welfareIds 需要删除的福利列表主键
     * @return 结果
     */
    @Override
    public int deleteTtWelfareByWelfareIds(Integer[] welfareIds)
    {
        return ttWelfareMapper.deleteTtWelfareByWelfareIds(welfareIds);
    }

    /**
     * 删除福利列表信息
     *
     * @param welfareId 福利列表主键
     * @return 结果
     */
    @Override
    public int deleteTtWelfareByWelfareId(Integer welfareId)
    {
        return ttWelfareMapper.deleteTtWelfareByWelfareId(welfareId);
    }
}
