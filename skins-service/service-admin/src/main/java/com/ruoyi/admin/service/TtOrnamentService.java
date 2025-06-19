package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.dto.queryCondition.OrnamentCondition;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.domain.entity.TtOrnamentsYY;
import com.ruoyi.domain.other.TtOrnamentsBody;
import com.ruoyi.domain.vo.TtOrnamentVO;
import com.ruoyi.domain.vo.upgrade.SimpleOrnamentVO;

import java.util.List;

public interface TtOrnamentService extends IService<TtOrnament> {

    Page<TtOrnamentVO> listByParam(TtOrnamentsBody param);

    List<TtOrnamentVO> selectTtOrnamentsList(TtOrnamentsBody param);

    List<String> selectOrnamentsItemIdList();

    AjaxResult grantOrnaments(Integer userId, Long ornamentsId, Integer ornamentsLevelId, Integer num);

    List<SimpleOrnamentVO> byCondition(OrnamentCondition condition);

    List<SimpleOrnamentVO> byCondition2(OrnamentCondition condition);

    List<Long> selectOrnamentsIdList();

    List<String> selectOrnamentsMarketHashNameList();
}
