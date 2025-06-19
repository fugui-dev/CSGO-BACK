package com.ruoyi.admin.mapper;

import java.util.List;
import com.ruoyi.domain.other.TtTaskCenter;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务中心Mapper接口
 *
 * @author ruoyi
 * @date 2024-05-25
 */
@Mapper
public interface TtTaskCenterMapper
{
    /**
     * 查询任务中心
     *
     * @param taskId 任务中心主键
     * @return 任务中心
     */
    public TtTaskCenter selectTtTaskCenterByTaskId(Integer taskId);

    /**
     * 查询任务中心列表
     *
     * @param ttTaskCenter 任务中心
     * @return 任务中心集合
     */
    public List<TtTaskCenter> selectTtTaskCenterList(TtTaskCenter ttTaskCenter);

    /**
     * 新增任务中心
     *
     * @param ttTaskCenter 任务中心
     * @return 结果
     */
    public int insertTtTaskCenter(TtTaskCenter ttTaskCenter);

    /**
     * 修改任务中心
     *
     * @param ttTaskCenter 任务中心
     * @return 结果
     */
    public int updateTtTaskCenter(TtTaskCenter ttTaskCenter);

    /**
     * 删除任务中心
     *
     * @param taskId 任务中心主键
     * @return 结果
     */
    public int deleteTtTaskCenterByTaskId(Integer taskId);

    /**
     * 批量删除任务中心
     *
     * @param taskIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTtTaskCenterByTaskIds(Integer[] taskIds);
}
