package com.ruoyi.admin.mapper;

import java.util.List;
import com.ruoyi.domain.other.TtWelfare;
import org.apache.ibatis.annotations.Mapper;

/**
 * 福利列表Mapper接口
 *
 * @author ruoyi
 * @date 2024-05-11
 */
@Mapper
public interface TtWelfareMapper
{
    /**
     * 查询福利列表
     *
     * @param welfareId 福利列表主键
     * @return 福利列表
     */
    public TtWelfare selectTtWelfareByWelfareId(Integer welfareId);

    /**
     * 查询福利列表列表
     *
     * @param ttWelfare 福利列表
     * @return 福利列表集合
     */
    public List<TtWelfare> selectTtWelfareList(TtWelfare ttWelfare);

    /**
     * 新增福利列表
     *
     * @param ttWelfare 福利列表
     * @return 结果
     */
    public int insertTtWelfare(TtWelfare ttWelfare);

    /**
     * 修改福利列表
     *
     * @param ttWelfare 福利列表
     * @return 结果
     */
    public int updateTtWelfare(TtWelfare ttWelfare);

    /**
     * 删除福利列表
     *
     * @param welfareId 福利列表主键
     * @return 结果
     */
    public int deleteTtWelfareByWelfareId(Integer welfareId);

    /**
     * 批量删除福利列表
     *
     * @param welfareIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTtWelfareByWelfareIds(Integer[] welfareIds);
}
