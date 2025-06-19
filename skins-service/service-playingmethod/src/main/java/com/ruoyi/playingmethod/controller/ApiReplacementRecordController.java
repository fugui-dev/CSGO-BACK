package com.ruoyi.playingmethod.controller;

import com.ruoyi.common.annotation.UserPermission;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.playingmethod.model.body.SynthesizeRequest;
import com.ruoyi.playingmethod.service.IApiReplacementRecordService;
import com.ruoyi.user.model.dto.SmeltRequest;
import com.ruoyi.user.model.vo.SmeltVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 汰换记录Controller
 *
 * @author junhai
 * @date 2023-09-10
 */
@RestController
@RequestMapping("/api/replacementRecord")
@Api(tags = "汰换玩法（熔炼）")
public class ApiReplacementRecordController extends BaseController {

    @Autowired
    private IApiReplacementRecordService replacementRecordService;

    @PostMapping("/synthesizeItems")
    @UserPermission
    @ApiOperation(value = "汰换饰品", hidden = true)
    public AjaxResult synthesizeItems(@RequestBody SynthesizeRequest synthesizeRequest) {
        LoginUser loginUser = getLoginUser();
        return replacementRecordService.synthesizeItems(loginUser, synthesizeRequest.getBoIds());
    }

    @ApiOperation("饰品熔炼/汰换")
    @PostMapping("/smelt")
    public R<SmeltVO> smelt(@RequestBody @Validated SmeltRequest smeltRequest) {
        return replacementRecordService.smelt(smeltRequest);
    }

}
