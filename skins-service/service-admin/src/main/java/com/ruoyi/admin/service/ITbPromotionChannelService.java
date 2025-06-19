package com.ruoyi.admin.service;

import java.util.List;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.domain.entity.TbPromotionChannel;

/**
 * 推广渠道通道Service接口
 * 
 * @author ruoyi
 * @date 2024-06-29
 */
public interface ITbPromotionChannelService 
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
     * 批量删除推广渠道通道
     * 
     * @param ids 需要删除的推广渠道通道主键集合
     * @return 结果
     */
    public int deleteTbPromotionChannelByIds(Long[] ids);

    /**
     * 删除推广渠道通道信息
     * 
     * @param id 推广渠道通道主键
     * @return 结果
     */
    public int deleteTbPromotionChannelById(Long id);

    /**
     * 查询渠道的报表数据
     * 总注册
     * 首充
     * 总充值
     * @param id
     * @return
     */
    AjaxResult reportInfo(Integer id);
}
