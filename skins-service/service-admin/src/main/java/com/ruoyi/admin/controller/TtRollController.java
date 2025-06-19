package com.ruoyi.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.admin.config.DeleteFlag;
import com.ruoyi.domain.dto.queryCondition.RollPrizesCondition;
import com.ruoyi.domain.dto.roll.GetRollPrizePool;
import com.ruoyi.domain.dto.roll.InviteRollUser;
import com.ruoyi.domain.entity.roll.TtRoll;
import com.ruoyi.domain.entity.roll.TtRollUser;
import com.ruoyi.admin.service.TtRollService;
import com.ruoyi.admin.service.TtRollUserService;
import com.ruoyi.domain.entity.roll.TtRollBody;
import com.ruoyi.domain.vo.TtRollPrizeDataVO;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.domain.vo.roll.RollJackpotOrnamentsByPageVO;
import com.ruoyi.domain.vo.roll.RollJackpotOrnamentsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "管理端 roll房")
@RestController
@RequestMapping("/admin/roll")
public class TtRollController extends BaseController {

    private final TtRollService rollService;
    private final TtRollUserService rollUserService;

    public TtRollController(TtRollService rollService,
                            TtRollUserService rollUserService) {
        this.rollService = rollService;
        this.rollUserService = rollUserService;
    }

    @GetMapping("/list")
    public PageDataInfo<TtRoll> list(TtRollBody ttRollBody) {
        startPage();
        List<TtRoll> list = rollService.queryList(ttRollBody);
        return getPageData(list);
    }

    /**
     * 导出信息
     * @param response
     * @param ttRollBody
     */
    @PostMapping("/export")
    public void export(HttpServletResponse response, TtRollBody ttRollBody) {
        List<TtRoll> list = rollService.queryList(ttRollBody);
        ExcelUtil<TtRoll> util = new ExcelUtil<>(TtRoll.class);
        util.exportExcel(response, list, "Roll房信息列表");
    }

    @GetMapping(value = "/{rollId}")
    public R<TtRoll> getInfo(@PathVariable("rollId") Long rollId) {
        TtRoll roll = rollService.getById(rollId);
        return R.ok(roll);
    }

    // 创建roll房
    @ApiOperation("创建roll房")
    @PostMapping
    public AjaxResult add(@RequestBody TtRoll ttRoll) {

        ttRoll.setCreateBy(getUsername());
        ttRoll.setCreateTime(DateUtils.getNowDate());
        return rollService.createRoll(ttRoll);

    }

    // roll房拉人
    @ApiOperation("roll房拉人")
    @PostMapping("/inviteRollUser")
    public R inviteRollUser(@RequestBody InviteRollUser InviteRollUser) {

        return rollService.inviteRollUser(InviteRollUser);

    }

    // 获取roll房成员信息
    @ApiOperation("获取roll房成员信息")
    @GetMapping("getRollUsers/{rollId}")
    public AjaxResult getRollUsers(@PathVariable("rollId") Integer rollId) {

        return rollService.getRollUsers(rollId);

    }


    @ApiOperation("编辑roll房")
    @PutMapping
    public AjaxResult edit(@RequestBody TtRoll ttRoll) {

        ttRoll.setUpdateBy(getUsername());
        ttRoll.setUpdateTime(DateUtils.getNowDate());

        return rollService.updateRollById(ttRoll);
    }

    @ApiOperation("删除roll房")
    @DeleteMapping("/remove/{rollId}")
    public AjaxResult remove(@PathVariable Long rollId) {
//        LambdaQueryWrapper<TtRollUser> wrapper = Wrappers.lambdaQuery();
//        wrapper.eq(TtRollUser::getRollId, rollId);
//        List<TtRollUser> list = rollUserService.list(wrapper);

//        TtRoll ttRoll = rollService.getById(rollId);
//        ttRoll.setUpdateBy(getUsername());
//        ttRoll.setUpdateTime(DateUtils.getNowDate());
//        ttRoll.setDelFlag(DeleteFlag.DEL);
//        return toAjax(rollService.updateById(ttRoll));

        LambdaUpdateWrapper<TtRoll> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TtRoll::getId, rollId);
        updateWrapper.set(TtRoll::getUpdateBy, getUsername());
        updateWrapper.set(TtRoll::getUpdateTime, DateUtils.getNowDate());
        updateWrapper.set(TtRoll::getDelFlag, DeleteFlag.DEL);
        boolean update = rollService.update(updateWrapper);

        if (update){
            return success();
        }

        return error();

//        if (StringUtils.isNull(list) || list.isEmpty()) return toAjax(rollService.removeById(rollId));
//        return AjaxResult.error("该Roll房内存在用户，无法删除！");

    }


    // @ApiOperation("roll房奖品列表")
    // @PostMapping("/getRollPrizeList")
    // public R<List<TtRollPrizeDataVO>> getRollPrizeList(@RequestBody RollPrizesCondition condition) {
    //
    //     List<TtRollPrizeDataVO> list = rollService.getRollPrizeList(condition);
    //
    //     return R.ok(list);
    //
    // }
    @ApiOperation("roll房奖品列表")
    @GetMapping("/getRollPrizeList/{rollId}")
    public R<List<TtRollPrizeDataVO>> getRollPrizeList(@PathVariable("rollId") Integer rollId) {

        List<TtRollPrizeDataVO> list = rollService.getRollPrizeList(rollId);

        return R.ok(list);

    }

    @ApiOperation("获取 roll房奖池详情")
    @PostMapping("/getRollPrizePool")
    public R<RollJackpotOrnamentsByPageVO> getRollPrizePool(@RequestBody @Validated GetRollPrizePool param) {
        return rollService.getRollPrizePool(param);
    }

    @ApiOperation("roll房的指定获奖人名单")
    @GetMapping("/rollWinners/{rollId}")
    public R rollWinners(@PathVariable Integer rollId) {
        return rollUserService.rollWinners(rollId);
    }

    @ApiOperation("指定roll房winner")
    @PostMapping("/namedWinner")
    public AjaxResult namedWinner(@RequestBody @Validated TtRollPrizeDataVO param) {
        return rollService.namedWinner(param);
    }

    @ApiOperation("取消指定roll房奖品")
    @DeleteMapping("/cancelNamedWinner")
    public R cancelNamedWinner(@RequestBody List<Integer> rollUserPrizeIds) {
        return rollService.cancelNamedWinner(rollUserPrizeIds);
    }
}
