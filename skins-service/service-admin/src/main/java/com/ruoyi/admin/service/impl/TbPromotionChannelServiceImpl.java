package com.ruoyi.admin.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import com.ruoyi.admin.mapper.TtRechargeRecordMapper;
import com.ruoyi.admin.mapper.TtUserMapper;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.sign.Md5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.admin.mapper.TbPromotionChannelMapper;
import com.ruoyi.domain.entity.TbPromotionChannel;
import com.ruoyi.admin.service.ITbPromotionChannelService;

/**
 * 推广渠道通道Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-06-29
 */
@Service
public class TbPromotionChannelServiceImpl implements ITbPromotionChannelService 
{
    @Autowired
    private TbPromotionChannelMapper tbPromotionChannelMapper;

    @Autowired
    private TtUserMapper userMapper;

    @Autowired
    private TtRechargeRecordMapper rechargeRecordMapper;

    /**
     * 查询推广渠道通道
     * 
     * @param id 推广渠道通道主键
     * @return 推广渠道通道
     */
    @Override
    public TbPromotionChannel selectTbPromotionChannelById(Long id)
    {
        return tbPromotionChannelMapper.selectTbPromotionChannelById(id);
    }

    /**
     * 查询推广渠道通道列表
     * 
     * @param tbPromotionChannel 推广渠道通道
     * @return 推广渠道通道
     */
    @Override
    public List<TbPromotionChannel> selectTbPromotionChannelList(TbPromotionChannel tbPromotionChannel)
    {
        return tbPromotionChannelMapper.selectTbPromotionChannelList(tbPromotionChannel);
    }

    /**
     * 新增推广渠道通道
     * 
     * @param tbPromotionChannel 推广渠道通道
     * @return 结果
     */
    @Override
    public int insertTbPromotionChannel(TbPromotionChannel tbPromotionChannel)
    {
        tbPromotionChannel.setCreateTime(DateUtils.getNowDate());
        return tbPromotionChannelMapper.insertTbPromotionChannel(tbPromotionChannel);
    }

    /**
     * 修改推广渠道通道
     * 
     * @param tbPromotionChannel 推广渠道通道
     * @return 结果
     */
    @Override
    public int updateTbPromotionChannel(TbPromotionChannel tbPromotionChannel)
    {
        tbPromotionChannel.setUpdateTime(DateUtils.getNowDate());
        //密码不为空时重置密码
        if (StringUtils.isNotBlank(tbPromotionChannel.getPassword())){
            tbPromotionChannel.setPassword(Md5Utils.hash(tbPromotionChannel.getPassword()));
        }
        return tbPromotionChannelMapper.updateTbPromotionChannel(tbPromotionChannel);
    }

    /**
     * 批量删除推广渠道通道
     * 
     * @param ids 需要删除的推广渠道通道主键
     * @return 结果
     */
    @Override
    public int deleteTbPromotionChannelByIds(Long[] ids)
    {
        return tbPromotionChannelMapper.deleteTbPromotionChannelByIds(ids);
    }

    /**
     * 删除推广渠道通道信息
     * 
     * @param id 推广渠道通道主键
     * @return 结果
     */
    @Override
    public int deleteTbPromotionChannelById(Long id)
    {
        return tbPromotionChannelMapper.deleteTbPromotionChannelById(id);
    }

    /**
     * 报表统计...
     * @param id
     * @return
     */
    @Override
    public AjaxResult reportInfo(Integer id) {

        //注册用户
        Integer registerNum = userMapper.getBdPromotionRegisterByChannelId(id);

        //付费用户（首充）
        Integer firstChargeNum = rechargeRecordMapper.getFirstChargeNumByChannelId(id);

        //总充值
        BigDecimal totalCharge = rechargeRecordMapper.getTotalChargeByChannelId(id);

        HashMap<String, Object> map = new HashMap<>(3);
        map.put("registerNum", registerNum);
        map.put("firstChargeNum", firstChargeNum);
        map.put("totalCharge", totalCharge);

        return AjaxResult.success(map);
    }
}
