package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.admin.controller.TtUpgradeFailOrnamentsController;
import com.ruoyi.admin.controller.TtUpgradeFailOrnamentsController.BatchAddParam;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.domain.other.TtUpgradeFailOrnaments;
import com.ruoyi.domain.vo.TtUpgradeFailOrnamentsDataVO;

import java.util.List;

public interface TtUpgradeFailOrnamentsService extends IService<TtUpgradeFailOrnaments> {

    List<TtUpgradeFailOrnamentsDataVO> queryList(TtUpgradeFailOrnamentsController.listParam param);

    AjaxResult batchAdd(BatchAddParam param);

    String updateUpgradeFailOrnamentsById(TtUpgradeFailOrnamentsDataVO ttUpgradeFailOrnamentsDataVO);
}
