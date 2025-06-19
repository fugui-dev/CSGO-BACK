package com.ruoyi.thirdparty.wechat.service;

import com.ruoyi.thirdparty.wechat.entity.TtUserLsjl;

/**
 * 流水记录Service接口
 * 
 * @author junhai
 * @date 2023-08-19
 */
public interface ITtUserLsjlService
{

    /**
     * 新增流水记录
     * 
     * @param ttUserLsjl 流水记录
     * @return 结果
     */
    public int insertTtUserLsjl(TtUserLsjl ttUserLsjl);
}
