package com.ruoyi.admin.controller;

import com.ruoyi.domain.entity.roll.TtTimeRoll;
import com.ruoyi.admin.service.TtTimeRollService;
import com.ruoyi.domain.vo.TtRollPrizeDataVO;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.exception.job.TaskException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import org.quartz.SchedulerException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/timeRoll")
public class TtTimeRollController extends BaseController {

    private final TtTimeRollService timeRollService;

    public TtTimeRollController(TtTimeRollService timeRollService) {
        this.timeRollService = timeRollService;
    }

    @GetMapping("/list")
    public PageDataInfo<TtTimeRoll> list(){
        startPage();
        List<TtTimeRoll> list = timeRollService.list();
        return getPageData(list);
    }

    @GetMapping(value = "/{timeRollId}")
    public R<TtTimeRoll> getInfo(@PathVariable("timeRollId") Integer timeRollId) {
        TtTimeRoll timeRoll = timeRollService.getById(timeRollId);
        return R.ok(timeRoll);
    }

    @PostMapping
    public AjaxResult add(@RequestBody TtTimeRoll ttTimeRoll) throws SchedulerException, TaskException {
        ttTimeRoll.setCreateBy(getUsername());
        ttTimeRoll.setCreateTime(DateUtils.getNowDate());
        String msg = timeRollService.insertTimeRoll(ttTimeRoll);
        return StringUtils.isEmpty(msg) ? AjaxResult.success("新增成功！") : AjaxResult.error(msg);
    }

    @PutMapping
    public AjaxResult edit(@RequestBody TtTimeRoll ttTimeRoll) {
        ttTimeRoll.setUpdateBy(getUsername());
        ttTimeRoll.setUpdateTime(DateUtils.getNowDate());
        return toAjax(timeRollService.updateById(ttTimeRoll));
    }

    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody TtTimeRoll ttTimeRoll) throws SchedulerException {
        String msg = timeRollService.changeStatus(ttTimeRoll);
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }

    @DeleteMapping("/remove/{id}")
    public AjaxResult remove(@PathVariable Integer id) throws SchedulerException {
        return timeRollService.removeTimeRollById(id);
    }

    @GetMapping("/getTimeRollPrizeList/{id}")
    public R<List<TtRollPrizeDataVO>> getTimeRollPrizeList(@PathVariable("id") Integer id) {
        List<TtRollPrizeDataVO> list = timeRollService.getTimeRollPrizeList(id);
        List<TtRollPrizeDataVO> sortedList = list.stream().sorted((a, b) -> b.getUsePrice().compareTo(a.getUsePrice())).collect(Collectors.toList());
        return R.ok(sortedList);
    }

    @PostMapping("/namedWinner")
    public AjaxResult namedWinner(@RequestBody TtRollPrizeDataVO rollPrizeData) {
        String msg = timeRollService.namedWinner(rollPrizeData);
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }

}
