package com.ruoyi.admin.controller;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.ruoyi.domain.other.TtUpgradeFailOrnaments;
import com.ruoyi.domain.other.TtUpgradeOrnaments;
import com.ruoyi.admin.service.TtUpgradeFailOrnamentsService;
import com.ruoyi.admin.service.TtUpgradeOrnamentsService;
import com.ruoyi.domain.other.TtUpgradeOrnamentsBody;
import com.ruoyi.domain.vo.TtUpgradeOrnamentsDataVO;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api(tags = "管理端 幸运升级")
@RestController
@RequestMapping("/admin/upgradeOrnaments")
public class TtUpgradeOrnamentsController extends BaseController {

    private final TtUpgradeFailOrnamentsService upgradeFailOrnamentsService;
    private final TtUpgradeOrnamentsService upgradeOrnamentsService;

    public TtUpgradeOrnamentsController(TtUpgradeFailOrnamentsService upgradeFailOrnamentsService,
                                        TtUpgradeOrnamentsService upgradeOrnamentsService) {
        this.upgradeFailOrnamentsService = upgradeFailOrnamentsService;
        this.upgradeOrnamentsService = upgradeOrnamentsService;
    }

    @ApiOperation("获取参与幸运升级的物品")
    @GetMapping("/list")
    public PageDataInfo<TtUpgradeOrnamentsDataVO> list(TtUpgradeOrnamentsBody ttUpgradeOrnamentsBody){
        startPage();
        List<TtUpgradeOrnamentsDataVO> list = upgradeOrnamentsService.queryList(ttUpgradeOrnamentsBody);
        return getPageData(list);
    }

    @GetMapping(value = "/{id}")
    public R<TtUpgradeOrnaments> getInfo(@PathVariable("id") Integer id) {
        TtUpgradeOrnaments ttUpgradeOrnaments = upgradeOrnamentsService.getById(id);
        return R.ok(ttUpgradeOrnaments);
    }

    @ApiOperation("批量填货")
    @PostMapping("/batchAdd/{ornamentsIds}")
    public AjaxResult batchAdd(@PathVariable Long[] ornamentsIds) {
        String msg = upgradeOrnamentsService.batchAdd(Arrays.asList(ornamentsIds));
        return StringUtils.isEmpty(msg) ? AjaxResult.success("批量上架成功，请手动修改幸运区间！") : AjaxResult.error(msg);
    }

    @PutMapping
    public AjaxResult edit(@RequestBody TtUpgradeOrnamentsDataVO ttUpgradeOrnamentsDataVO) {
        String msg = upgradeOrnamentsService.updateUpgradeOrnamentsById(ttUpgradeOrnamentsDataVO);
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids) {
        List<TtUpgradeFailOrnaments> upgradeFailOrnamentsList = new LambdaQueryChainWrapper<>(upgradeFailOrnamentsService.getBaseMapper())
                .in(TtUpgradeFailOrnaments::getUpgradeId, Arrays.asList(ids)).list();
        List<Integer> list = upgradeFailOrnamentsList.stream().map(TtUpgradeFailOrnaments::getId).collect(Collectors.toList());
        upgradeFailOrnamentsService.removeByIds(list);
        return toAjax(upgradeOrnamentsService.removeByIds(Arrays.asList(ids)));
    }

    @GetMapping("/getUpgradeProfitStatistics/{id}")
    public R<Object> getUpgradeProfitStatistics(@PathVariable("id") Integer id){
        Map<String, BigDecimal> map = upgradeOrnamentsService.getUpgradeProfitStatistics(id);
        return R.ok(map);
    }

}
