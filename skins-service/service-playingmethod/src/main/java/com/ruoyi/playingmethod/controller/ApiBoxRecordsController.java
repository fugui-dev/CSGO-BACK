package com.ruoyi.playingmethod.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.dto.boxRecords.queryCondition;
import com.ruoyi.domain.vo.boxRecords.TtBoxRecordsVO;
import com.ruoyi.playingmethod.service.ApiBoxRecordsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Api(tags = "出货记录")
@RestController
@RequestMapping("/api/boxRecords")
public class ApiBoxRecordsController extends BaseController {

    @Autowired
    private ApiBoxRecordsService apiBoxRecordsService;

    @ApiOperation("获取历史出货记录")
    @PostMapping("/historyByCondition")
    public R<List<TtBoxRecordsVO>> getBindBoxHistory(@RequestBody @Validated queryCondition param) {
        if (param.getStatus().contains(1) || param.getStatus().contains(10)) {
            return R.fail("非法的状态参数。");
        }

        List<TtBoxRecordsVO> ttBoxRecordsVOS = apiBoxRecordsService.byCondition(param);
        return R.ok(ttBoxRecordsVOS);
    }

    @ApiOperation("我的出货记录")
    @PostMapping("/myRecord")
    public R<List<TtBoxRecordsVO>> myRecord(@RequestBody @Validated queryCondition param) {
        //增加userId
        Long userId = getUserId();
        ArrayList<Integer> source = new ArrayList<>(1);
        source.add(1); //设置为开箱

        param.setSource(source);
        param.setUserId(userId.intValue());

        List<TtBoxRecordsVO> ttBoxRecordsVOS = apiBoxRecordsService.byCondition(param);
        return R.ok(ttBoxRecordsVOS);
    }

}
