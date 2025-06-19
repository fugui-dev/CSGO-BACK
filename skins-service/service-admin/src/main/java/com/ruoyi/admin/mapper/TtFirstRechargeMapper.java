package com.ruoyi.admin.mapper;

import com.ruoyi.domain.other.TtFirstRecharge;

import java.math.BigDecimal;
import java.util.List;

/**
 * 首充赠送Mapper接口
 *
 * @author ruoyi
 * @date 2024-06-21
 */
public interface TtFirstRechargeMapper
{
    /**
     * 查询首充赠送
     *
     * @param id 首充赠送主键
     * @return 首充赠送
     */
    public TtFirstRecharge selectTtFirstRechargeById(Integer id);

    /**
     * 查询首充赠送列表
     *
     * @param ttFirstRecharge 首充赠送
     * @return 首充赠送集合
     */
    public List<TtFirstRecharge> selectTtFirstRechargeList(TtFirstRecharge ttFirstRecharge);

    /**
     * 新增首充赠送
     *
     * @param ttFirstRecharge 首充赠送
     * @return 结果
     */
    public int insertTtFirstRecharge(TtFirstRecharge ttFirstRecharge);

    /**
     * 修改首充赠送
     *
     * @param ttFirstRecharge 首充赠送
     * @return 结果
     */
    public int updateTtFirstRecharge(TtFirstRecharge ttFirstRecharge);

    /**
     * 删除首充赠送
     *
     * @param id 首充赠送主键
     * @return 结果
     */
    public int deleteTtFirstRechargeById(Integer id);

    /**
     * 批量删除首充赠送
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTtFirstRechargeByIds(Integer[] ids);

    /**
     * 查询赠送比例
     */
    public BigDecimal selectRatioByMinAmount(BigDecimal minAmount);
}
