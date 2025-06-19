package com.ruoyi.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.admin.service.TtUpgradeRecordService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.dto.upgrade.UpgradeCondition;
import com.ruoyi.domain.other.TtUpgradeRecord;
import com.ruoyi.domain.other.TtUpgradeRecordBody;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.domain.vo.upgrade.UpgradeRecordVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "管理端 幸运升级日志")
@RestController
@RequestMapping("/admin/upgradeRecord")
public class TtUpgradeRecordController extends BaseController {

    private final TtUpgradeRecordService upgradeRecordService;

    public TtUpgradeRecordController(TtUpgradeRecordService upgradeRecordService) {
        this.upgradeRecordService = upgradeRecordService;
    }

    @ApiOperation("幸运升级日志")
    @PostMapping("/list")
    public R<Page<UpgradeRecordVO>> list(@RequestBody @Validated UpgradeCondition param){
        return upgradeRecordService.adminGetLog(param);
    }
}
