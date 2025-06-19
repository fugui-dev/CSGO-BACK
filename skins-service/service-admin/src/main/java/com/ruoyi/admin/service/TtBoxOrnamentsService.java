package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.admin.controller.TtBoxOrnamentsController.batchAddParam;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.other.TtBoxOrnaments;
import com.ruoyi.domain.vo.TtBoxOrnamentsDataVO;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.domain.vo.upgrade.SimpleOrnamentVO;

import java.util.List;

public interface TtBoxOrnamentsService extends IService<TtBoxOrnaments> {
    List<TtBoxOrnamentsDataVO> selectTtBoxOrnamentsList(Integer boxId);
    String saveBoxOrnaments(TtBoxOrnaments ttBoxOrnaments);
    String updateBoxOrnamentsById(TtBoxOrnamentsDataVO ttBoxOrnamentsDataVO);
    String removeBoxOrnamentsByIds(Integer boxId, List<Long> list);
    AjaxResult getProfitMargin(Integer boxId);
    String batchAdd(Integer boxId, List<Long> ornamentsIds);

    AjaxResult batchAdd(batchAddParam param);

    List<SimpleOrnamentVO> simpleBoxDetail(Integer boxId);

    R globalData(Integer boxId);
}
