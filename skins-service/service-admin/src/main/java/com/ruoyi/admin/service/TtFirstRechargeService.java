package com.ruoyi.admin.service;

import java.util.List;
import com.ruoyi.domain.other.TtFirstRecharge;

/**
 * 首充赠送Service接口
 *
 * @author ruoyi
 * @date 2024-06-21
 */
public interface TtFirstRechargeService
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
     * 批量删除首充赠送
     *
     * @param ids 需要删除的首充赠送主键集合
     * @return 结果
     */
    public int deleteTtFirstRechargeByIds(Integer[] ids);

    /**
     * 删除首充赠送信息
     *
     * @param id 首充赠送主键
     * @return 结果
     */
    public int deleteTtFirstRechargeById(Integer id);
}
