package com.ruoyi.admin.controller;


import com.ruoyi.admin.service.TtUserBlendErcashService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.dto.userRecord.BlendErcashCondition;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "管理端 收支明细")
@RestController
@RequestMapping("/admin/blendErcash")
@Slf4j
public class TtUserBlendErcashController extends BaseController {

    @Autowired
    private TtUserBlendErcashService ttUserBlendErcashService;

    @ApiOperation("收支明细")
    @PostMapping("/list")
    public R list(@RequestBody @Validated BlendErcashCondition condition) {
        return ttUserBlendErcashService.byCondition(condition);
    }

}
