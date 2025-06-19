package com.ruoyi.playingmethod.mapper;

import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.domain.other.TtReplacementRecord;
import com.ruoyi.domain.vo.UserPackSackDataVO;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 汰换记录Mapper接口
 *
 * @author junhai
 * @date 2023-09-10
 */
public interface ApiReplacementRecordMapper {
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

    /**
     * 根据以下参数进行饰品的查询
     *
     * @param map
     * @return
     */
    List<TtOrnament> findByPriceRange(Map<String, BigDecimal> map);

    /**
     * 查询背包饰品
     */
    List<UserPackSackDataVO> selectUserPackSack(@Param("userId") Integer userId,
                                                 @Param("itemIds") List<Long> itemIds);
}
