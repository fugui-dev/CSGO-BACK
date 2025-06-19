package com.ruoyi.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageHelper;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.PageUtils;
import com.ruoyi.domain.entity.recorde.TtUserAmountRecords;
import com.ruoyi.admin.service.TtUserAmountRecordsService;
import com.ruoyi.domain.other.TtUserAmountRecordsBody;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.PageDataInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "管理端 金币账户")
@RestController
@RequestMapping("/admin/userAmountRecords")
public class TtUserAmountRecordsController extends BaseController {

    private final TtUserAmountRecordsService userAmountRecordsService;

    public TtUserAmountRecordsController(TtUserAmountRecordsService userAmountRecordsService) {
        this.userAmountRecordsService = userAmountRecordsService;
    }

    @ApiOperation("获取列表")
    @PostMapping("/list")
    public TableDataInfo list(@RequestBody @Validated TtUserAmountRecordsBody ttUserAmountRecordsBody) {
        PageHelper.startPage(ttUserAmountRecordsBody.getPageNum(), ttUserAmountRecordsBody.getPageSize());
        return getDataTable(userAmountRecordsService.queryList(ttUserAmountRecordsBody));
    }
}
