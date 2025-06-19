package com.ruoyi.admin.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.domain.entity.TtPayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.admin.mapper.TtPayConfigMapper;
import com.ruoyi.admin.service.ITtPayConfigService;

/**
 * 支付配置Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-06-25
 */
@Service
public class TtPayConfigServiceImpl implements ITtPayConfigService 
{
    @Autowired
    private TtPayConfigMapper ttPayConfigMapper;

    /**
     * 查询支付配置
     * 
     * @param id 支付配置主键
     * @return 支付配置
     */
    @Override
    public TtPayConfig selectTtPayConfigById(Long id)
    {
        return ttPayConfigMapper.selectTtPayConfigById(id);
    }

    /**
     * 查询支付配置列表
     * 
     * @param ttPayConfig 支付配置
     * @return 支付配置
     */
    @Override
    public List<TtPayConfig> selectTtPayConfigList(TtPayConfig ttPayConfig)
    {
        return ttPayConfigMapper.selectTtPayConfigList(ttPayConfig);
    }

    /**
     * 新增支付配置
     * 
     * @param ttPayConfig 支付配置
     * @return 结果
     */
    @Override
    public int insertTtPayConfig(TtPayConfig ttPayConfig)
    {
        return ttPayConfigMapper.insertTtPayConfig(ttPayConfig);
    }

    /**
     * 修改支付配置
     * 
     * @param ttPayConfig 支付配置
     * @return 结果
     */
    @Override
    public int updateTtPayConfig(TtPayConfig ttPayConfig)
    {
        ttPayConfig.setUpdateTime(DateUtils.getNowDate());
        return ttPayConfigMapper.updateTtPayConfig(ttPayConfig);
    }

    /**
     * 批量删除支付配置
     * 
     * @param ids 需要删除的支付配置主键
     * @return 结果
     */
    @Override
    public int deleteTtPayConfigByIds(Long[] ids)
    {
        return ttPayConfigMapper.deleteTtPayConfigByIds(ids);
    }

    /**
     * 删除支付配置信息
     * 
     * @param id 支付配置主键
     * @return 结果
     */
    @Override
    public int deleteTtPayConfigById(Long id)
    {
        return ttPayConfigMapper.deleteTtPayConfigById(id);
    }
}
