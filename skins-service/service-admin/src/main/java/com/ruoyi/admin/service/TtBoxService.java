package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.other.TtBox;
import com.ruoyi.domain.other.TtBoxType;
import com.ruoyi.domain.vo.BoxCacheDataVO;
import com.ruoyi.domain.other.TtBoxBody;
import com.ruoyi.domain.vo.TtBoxDataVO;
import com.ruoyi.common.core.page.PageDataInfo;

import java.util.Date;
import java.util.List;

public interface TtBoxService extends IService<TtBox> {

    PageDataInfo<TtBoxDataVO> selectTtBoxList(TtBoxBody ttBoxBody);

    String updateTtBoxById(TtBoxDataVO ttBoxDataVO);

    void isReplenishment(Integer boxId);

    void delCache(Integer boxId);

    BoxCacheDataVO statisticsBoxData(Integer boxId, Date date);

    List<Long> getRealList(Integer boxId, int flag);

}
