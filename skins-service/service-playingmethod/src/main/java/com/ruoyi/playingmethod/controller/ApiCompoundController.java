package com.ruoyi.playingmethod.controller;

import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.playingmethod.service.ApiCompoundService;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.domain.vo.UserPackSackDataVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "合成")
@RestController
@RequestMapping("/api/compound")
public class ApiCompoundController extends BaseController {

    private final ISysConfigService sysConfigService;
    private final TtUserService userService;
    private final ApiCompoundService compoundService;

    public ApiCompoundController(ISysConfigService sysConfigService,
                                 TtUserService userService,
                                 ApiCompoundService compoundService) {
        this.sysConfigService = sysConfigService;
        this.userService = userService;
        this.compoundService = compoundService;
    }

    @ApiOperation("合成")
    @PostMapping("/compound")
    public R<UserPackSackDataVO> compound(@RequestBody List<Long> packSackIds) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        String compoundMaintenance = sysConfigService.selectConfigByKey("compoundMaintenance");
        if ("1".equals(compoundMaintenance)) {
            return R.fail("汰换合成功能正在维护中......");
        }
        TtUser ttUser = userService.getById(getUserId());
        return compoundService.compound(packSackIds, ttUser);
    }

    @ApiOperation("获取用户合成记录")
    @GetMapping("/getUserCompoundRecord")
    public PageDataInfo<UserPackSackDataVO> getUserCompoundRecord() {
        startPage();
        List<UserPackSackDataVO> userCompoundRecord = compoundService.getUserCompoundRecord(getUserId().intValue());
        return getPageData(userCompoundRecord);
    }
}
