package com.ruoyi.admin.service.impl;

import com.ruoyi.admin.mapper.TtReplacementRecordMapper;
import com.ruoyi.admin.service.TtReplacementRecordService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.domain.other.TtReplacementRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 汰换记录Service业务层处理
 * 
 * @author junhai
 * @date 2023-09-10
 */
@Service
public class TtReplacementRecordServiceImpl implements TtReplacementRecordService
{
    @Autowired
    private TtReplacementRecordMapper ttReplacementRecordMapper;

    /**
     * 查询汰换记录
     * 
     * @param id 汰换记录主键
     * @return 汰换记录
     */
    @Override
    public TtReplacementRecord selectTtReplacementRecordById(Long id)
    {
        return ttReplacementRecordMapper.selectTtReplacementRecordById(id);
    }

    /**
     * 查询汰换记录列表
     * 
     * @param ttReplacementRecord 汰换记录
     * @return 汰换记录
     */
    @Override
    public List<TtReplacementRecord> selectTtReplacementRecordList(TtReplacementRecord ttReplacementRecord)
    {
        return ttReplacementRecordMapper.selectTtReplacementRecordList(ttReplacementRecord);
    }

    /**
     * 新增汰换记录
     * 
     * @param ttReplacementRecord 汰换记录
     * @return 结果
     */
    @Override
    public int insertTtReplacementRecord(TtReplacementRecord ttReplacementRecord)
    {
        ttReplacementRecord.setCreateTime(DateUtils.getNowDate());
        return ttReplacementRecordMapper.insertTtReplacementRecord(ttReplacementRecord);
    }

    /**
     * 修改汰换记录
     * 
     * @param ttReplacementRecord 汰换记录
     * @return 结果
     */
    @Override
    public int updateTtReplacementRecord(TtReplacementRecord ttReplacementRecord)
    {
        ttReplacementRecord.setUpdateTime(DateUtils.getNowDate());
        return ttReplacementRecordMapper.updateTtReplacementRecord(ttReplacementRecord);
    }

    /**
     * 批量删除汰换记录
     * 
     * @param ids 需要删除的汰换记录主键
     * @return 结果
     */
    @Override
    public int deleteTtReplacementRecordByIds(Long[] ids)
    {
        return ttReplacementRecordMapper.deleteTtReplacementRecordByIds(ids);
    }

    /**
     * 删除汰换记录信息
     * 
     * @param id 汰换记录主键
     * @return 结果
     */
    @Override
    public int deleteTtReplacementRecordById(Long id)
    {
        return ttReplacementRecordMapper.deleteTtReplacementRecordById(id);
    }
}
