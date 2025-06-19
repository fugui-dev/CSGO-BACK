package com.ruoyi.admin.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.admin.mapper.TtTaskCenterMapper;
import com.ruoyi.domain.other.TtTaskCenter;
import com.ruoyi.admin.service.TtTaskCenterService;

/**
 * 任务中心Service业务层处理
 *
 * @author ruoyi
 * @date 2024-05-25
 */
@Service
public class TtTaskCenterServiceImpl implements TtTaskCenterService
{
    @Autowired
    private TtTaskCenterMapper ttTaskCenterMapper;

    /**
     * 查询任务中心
     *
     * @param taskId 任务中心主键
     * @return 任务中心
     */
    @Override
    public TtTaskCenter selectTtTaskCenterByTaskId(Integer taskId)
    {
        return ttTaskCenterMapper.selectTtTaskCenterByTaskId(taskId);
    }

    /**
     * 查询任务中心列表
     *
     * @param ttTaskCenter 任务中心
     * @return 任务中心
     */
    @Override
    public List<TtTaskCenter> selectTtTaskCenterList(TtTaskCenter ttTaskCenter)
    {
        return ttTaskCenterMapper.selectTtTaskCenterList(ttTaskCenter);
    }

    /**
     * 新增任务中心
     *
     * @param ttTaskCenter 任务中心
     * @return 结果
     */
    @Override
    public int insertTtTaskCenter(TtTaskCenter ttTaskCenter)
    {
        ttTaskCenter.setCreateTime(DateUtils.getNowDate());
        return ttTaskCenterMapper.insertTtTaskCenter(ttTaskCenter);
    }

    /**
     * 修改任务中心
     *
     * @param ttTaskCenter 任务中心
     * @return 结果
     */
    @Override
    public int updateTtTaskCenter(TtTaskCenter ttTaskCenter)
    {
        ttTaskCenter.setUpdateTime(DateUtils.getNowDate());
        return ttTaskCenterMapper.updateTtTaskCenter(ttTaskCenter);
    }

    /**
     * 批量删除任务中心
     *
     * @param taskIds 需要删除的任务中心主键
     * @return 结果
     */
    @Override
    public int deleteTtTaskCenterByTaskIds(Integer[] taskIds)
    {
        return ttTaskCenterMapper.deleteTtTaskCenterByTaskIds(taskIds);
    }

    /**
     * 删除任务中心信息
     *
     * @param taskId 任务中心主键
     * @return 结果
     */
    @Override
    public int deleteTtTaskCenterByTaskId(Integer taskId)
    {
        return ttTaskCenterMapper.deleteTtTaskCenterByTaskId(taskId);
    }
}
