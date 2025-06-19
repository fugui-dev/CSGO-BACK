package com.ruoyi.admin.controller;

import com.ruoyi.admin.util.core.fight.LotteryMachine;
import com.ruoyi.domain.other.TtBox;
import com.ruoyi.admin.service.TtBoxService;
import com.ruoyi.domain.vo.BoxCacheDataVO;
import com.ruoyi.domain.other.TtBoxBody;
import com.ruoyi.domain.vo.TtBoxDataVO;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;

@Api(tags = "管理端 宝箱管理")
@RestController
@RequestMapping("/admin/box")
public class TtBoxController extends BaseController {

    @Autowired
    private TtBoxService boxService;

    @Autowired
    private LotteryMachine lotteryMachine;

    @ApiOperation("获取宝箱列表")
    @GetMapping("/list")
    public PageDataInfo<TtBoxDataVO> list(TtBoxBody ttBoxBody) {
        startPage();
        return boxService.selectTtBoxList(ttBoxBody);
    }

    @GetMapping(value = "/{boxId}")
    public R<TtBox> getInfo(@PathVariable("boxId") Long boxId) {
        TtBox ttBox = boxService.getById(boxId);
//        ttBox.setBoxImg01("");
//        ttBox.setBoxImg02("");
        return R.ok(ttBox);
    }

    @PostMapping
    public AjaxResult add(@RequestBody TtBox ttBox) {
        if (StringUtils.isEmpty(ttBox.getBoxImg01())) ttBox.setBoxImg01("");
        else ttBox.setBoxImg01(RuoYiConfig.getDomainName() + ttBox.getBoxImg01());
        if (StringUtils.isEmpty(ttBox.getBoxImg02())) ttBox.setBoxImg02("");
        else ttBox.setBoxImg02(RuoYiConfig.getDomainName() + ttBox.getBoxImg02());
        ttBox.setCreateBy(getUsername());
        ttBox.setCreateTime(DateUtils.getNowDate());
        return toAjax(boxService.save(ttBox));
    }

    @ApiOperation("修改宝箱")
    @PutMapping
    public AjaxResult edit(@RequestBody TtBoxDataVO ttBoxDataVO) {
        String msg = boxService.updateTtBoxById(ttBoxDataVO);
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }

    @ApiOperation("清空当前奖池")
    @GetMapping("clearPrizePool/{boxId}")
    public R clearPrizePool(@PathVariable("boxId") Integer boxId) {

        boolean b = lotteryMachine.clearBoxPrizePool(boxId);

        return R.ok("清空当前奖池成功。");
    }

    @DeleteMapping("/{boxIds}")
    public AjaxResult remove(@PathVariable Long[] boxIds) {
        return toAjax(boxService.removeByIds(Arrays.asList(boxIds)));
    }

    @GetMapping("/resetBox/{boxId}")
    public AjaxResult resetBox(@PathVariable Integer boxId) {
        boxService.delCache(boxId);
        boxService.isReplenishment(boxId);
        return AjaxResult.success();
    }

    @GetMapping("/statisticsBoxData/{boxId}")
    public R<BoxCacheDataVO> statisticsBoxData(@PathVariable Integer boxId, @RequestParam(required = false) Date date) {
        BoxCacheDataVO boxCacheData = boxService.statisticsBoxData(boxId, date);
        return R.ok(boxCacheData);
    }
}
