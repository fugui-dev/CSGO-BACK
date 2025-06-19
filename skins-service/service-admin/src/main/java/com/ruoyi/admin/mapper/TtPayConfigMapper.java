package com.ruoyi.admin.mapper;

import com.ruoyi.domain.entity.TtPayConfig;

import java.util.List;

/**
 * 支付配置Mapper接口
 * 
 * @author ruoyi
 * @date 2024-06-25
 */
public interface TtPayConfigMapper 
{
    /**
     * 查询支付配置
     * 
     * @param id 支付配置主键
     * @return 支付配置
     */
    public TtPayConfig selectTtPayConfigById(Long id);

    /**
     * 查询支付配置列表
     * 
     * @param ttPayConfig 支付配置
     * @return 支付配置集合
     */
    public List<TtPayConfig> selectTtPayConfigList(TtPayConfig ttPayConfig);

    /**
     * 新增支付配置
     * 
     * @param ttPayConfig 支付配置
     * @return 结果
     */
    public int insertTtPayConfig(TtPayConfig ttPayConfig);

    /**
     * 修改支付配置
     * 
     * @param ttPayConfig 支付配置
     * @return 结果
     */
    public int updateTtPayConfig(TtPayConfig ttPayConfig);

    /**
     * 删除支付配置
     * 
     * @param id 支付配置主键
     * @return 结果
     */
    public int deleteTtPayConfigById(Long id);

    /**
     * 批量删除支付配置
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTtPayConfigByIds(Long[] ids);
}
