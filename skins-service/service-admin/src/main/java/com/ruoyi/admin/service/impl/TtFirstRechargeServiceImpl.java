package com.ruoyi.admin.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.admin.mapper.TtFirstRechargeMapper;
import com.ruoyi.domain.other.TtFirstRecharge;
import com.ruoyi.admin.service.TtFirstRechargeService;

/**
 * 首充赠送Service业务层处理
 *
 * @author ruoyi
 * @date 2024-06-21
 */
@Service
public class TtFirstRechargeServiceImpl implements TtFirstRechargeService
{
    @Autowired
    private TtFirstRechargeMapper ttFirstRechargeMapper;

    /**
     * 查询首充赠送
     *
     * @param id 首充赠送主键
     * @return 首充赠送
     */
    @Override
    public TtFirstRecharge selectTtFirstRechargeById(Integer id)
    {
        return ttFirstRechargeMapper.selectTtFirstRechargeById(id);
    }

    /**
     * 查询首充赠送列表
     *
     * @param ttFirstRecharge 首充赠送
     * @return 首充赠送
     */
    @Override
    public List<TtFirstRecharge> selectTtFirstRechargeList(TtFirstRecharge ttFirstRecharge)
    {
        return ttFirstRechargeMapper.selectTtFirstRechargeList(ttFirstRecharge);
    }

    /**
     * 新增首充赠送
     *
     * @param ttFirstRecharge 首充赠送
     * @return 结果
     */
    @Override
    public int insertTtFirstRecharge(TtFirstRecharge ttFirstRecharge)
    {
        ttFirstRecharge.setCreateTime(DateUtils.getNowDate());
        return ttFirstRechargeMapper.insertTtFirstRecharge(ttFirstRecharge);
    }

    /**
     * 修改首充赠送
     *
     * @param ttFirstRecharge 首充赠送
     * @return 结果
     */
    @Override
    public int updateTtFirstRecharge(TtFirstRecharge ttFirstRecharge)
    {
        ttFirstRecharge.setUpdateTime(DateUtils.getNowDate());
        return ttFirstRechargeMapper.updateTtFirstRecharge(ttFirstRecharge);
    }

    /**
     * 批量删除首充赠送
     *
     * @param ids 需要删除的首充赠送主键
     * @return 结果
     */
    @Override
    public int deleteTtFirstRechargeByIds(Integer[] ids)
    {
        return ttFirstRechargeMapper.deleteTtFirstRechargeByIds(ids);
    }

    /**
     * 删除首充赠送信息
     *
     * @param id 首充赠送主键
     * @return 结果
     */
    @Override
    public int deleteTtFirstRechargeById(Integer id)
    {
        return ttFirstRechargeMapper.deleteTtFirstRechargeById(id);
    }
}
