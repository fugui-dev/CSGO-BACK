package com.ruoyi.admin.controller;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.ruoyi.domain.dto.rollJackpotOrnament.RollJOEdit;
import com.ruoyi.domain.entity.roll.TtRoll;
import com.ruoyi.domain.entity.roll.TtRollJackpotOrnaments;
import com.ruoyi.admin.service.TtRollJackpotOrnamentsService;
import com.ruoyi.admin.service.TtRollService;
import com.ruoyi.domain.entity.roll.TtRollJackpotOrnamentsBody;
import com.ruoyi.domain.vo.TtRollJackpotOrnamentsDataVO;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Api(tags = "管理端 roll奖池物品管理")
@RestController
@RequestMapping("/admin/rollJackpotOrnaments")
public class TtRollJackpotOrnamentsController extends BaseController {

    private final TtRollService rollService;
    private final TtRollJackpotOrnamentsService rollJackpotOrnamentsService;

    public TtRollJackpotOrnamentsController(TtRollService rollService,
                                            TtRollJackpotOrnamentsService rollJackpotOrnamentsService) {
        this.rollService = rollService;
        this.rollJackpotOrnamentsService = rollJackpotOrnamentsService;
    }

    @ApiOperation("物品列表")
    @GetMapping("/list")
    public PageDataInfo<TtRollJackpotOrnamentsDataVO> list(TtRollJackpotOrnamentsBody rollJackpotOrnamentsBody) {
        startPage();
        List<TtRollJackpotOrnamentsDataVO> list = rollJackpotOrnamentsService.queryList(rollJackpotOrnamentsBody);
        return getPageData(list);
    }

    @GetMapping(value = "/{rollJackpotOrnamentsId}")
    public R<TtRollJackpotOrnaments> getInfo(@PathVariable("rollJackpotOrnamentsId") Long rollJackpotOrnamentsId) {
        TtRollJackpotOrnaments rollJackpotOrnaments = rollJackpotOrnamentsService.getById(rollJackpotOrnamentsId);
        return R.ok(rollJackpotOrnaments);
    }

    @PostMapping
    public AjaxResult add(@RequestBody TtRollJackpotOrnaments ttRollJackpotOrnaments) {
        String msg = rollJackpotOrnamentsService.insertRollJackpotOrnaments(ttRollJackpotOrnaments);
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }

    @ApiOperation("修改奖池道具")
    @PutMapping
    public AjaxResult edit(@RequestBody TtRollJackpotOrnaments rollJOEdit) {
        String msg = rollJackpotOrnamentsService.updateRollJackpotOrnamentsById(rollJOEdit);
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }

    @DeleteMapping("/{jackpotId}/{rollJackpotOrnamentsIds}")
    public AjaxResult remove(@PathVariable("jackpotId") Integer jackpotId,
                             @PathVariable("rollJackpotOrnamentsIds") Long[] rollJackpotOrnamentsIds) {
        List<TtRoll> rollList = new LambdaQueryChainWrapper<>(rollService.getBaseMapper()).eq(TtRoll::getJackpotId, jackpotId)
                .eq(TtRoll::getRollStatus, "0").eq(TtRoll::getDelFlag, "0").list();
        if (StringUtils.isNotNull(rollList) && !rollList.isEmpty())
            return AjaxResult.error("该奖池正在被进行中的Roll房使用，禁止删除奖品！");
        return toAjax(rollJackpotOrnamentsService.removeByIds(Arrays.asList(rollJackpotOrnamentsIds)));
    }

    @ApiOperation("奖池批量填货")
    @PostMapping("/batchAdd/{rollJackpotId}/{OrnamentsIds}")
    public AjaxResult batchAdd(@PathVariable Integer rollJackpotId, @PathVariable Long[] OrnamentsIds) {
        String msg = rollJackpotOrnamentsService.batchAdd(rollJackpotId, Arrays.asList(OrnamentsIds));
        return StringUtils.isEmpty(msg) ? AjaxResult.success("批量填货成功，请手动修改饰品数量！") : AjaxResult.error(msg);
    }
}
