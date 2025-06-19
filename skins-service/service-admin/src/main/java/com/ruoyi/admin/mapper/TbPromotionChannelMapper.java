package com.ruoyi.admin.mapper;

import java.util.List;
import com.ruoyi.domain.entity.TbPromotionChannel;

/**
 * 推广渠道通道Mapper接口
 * 
 * @author ruoyi
 * @date 2024-06-29
 */
public interface TbPromotionChannelMapper 
{
    /**
     * 查询推广渠道通道
     * 
     * @param id 推广渠道通道主键
     * @return 推广渠道通道
     */
    public TbPromotionChannel selectTbPromotionChannelById(Long id);

    /**
     * 查询推广渠道通道列表
     * 
     * @param tbPromotionChannel 推广渠道通道
     * @return 推广渠道通道集合
     */
    public List<TbPromotionChannel> selectTbPromotionChannelList(TbPromotionChannel tbPromotionChannel);

    /**
     * 新增推广渠道通道
     * 
     * @param tbPromotionChannel 推广渠道通道
     * @return 结果
     */
    public int insertTbPromotionChannel(TbPromotionChannel tbPromotionChannel);

    /**
     * 修改推广渠道通道
     * 
     * @param tbPromotionChannel 推广渠道通道
     * @return 结果
     */
    public int updateTbPromotionChannel(TbPromotionChannel tbPromotionChannel);

    /**
     * 删除推广渠道通道
     * 
     * @param id 推广渠道通道主键
     * @return 结果
     */
    public int deleteTbPromotionChannelById(Long id);

    /**
     * 批量删除推广渠道通道
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTbPromotionChannelByIds(Long[] ids);
}
