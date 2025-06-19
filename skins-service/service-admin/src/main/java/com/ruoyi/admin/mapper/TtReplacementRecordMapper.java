package com.ruoyi.admin.mapper;

import com.ruoyi.domain.other.TtReplacementRecord;

import java.util.List;

/**
 * 汰换记录Mapper接口
 * 
 * @author junhai
 * @date 2023-09-10
 */
public interface TtReplacementRecordMapper 
{
    /**
     * 查询汰换记录
     * 
     * @param id 汰换记录主键
     * @return 汰换记录
     */
    public TtReplacementRecord selectTtReplacementRecordById(Long id);

    /**
     * 查询汰换记录列表
     * 
     * @param ttReplacementRecord 汰换记录
     * @return 汰换记录集合
     */
    public List<TtReplacementRecord> selectTtReplacementRecordList(TtReplacementRecord ttReplacementRecord);

    /**
     * 新增汰换记录
     * 
     * @param ttReplacementRecord 汰换记录
     * @return 结果
     */
    public int insertTtReplacementRecord(TtReplacementRecord ttReplacementRecord);

    /**
     * 修改汰换记录
     * 
     * @param ttReplacementRecord 汰换记录
     * @return 结果
     */
    public int updateTtReplacementRecord(TtReplacementRecord ttReplacementRecord);

    /**
     * 删除汰换记录
     * 
     * @param id 汰换记录主键
     * @return 结果
     */
    public int deleteTtReplacementRecordById(Long id);

    /**
     * 批量删除汰换记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTtReplacementRecordByIds(Long[] ids);
}
