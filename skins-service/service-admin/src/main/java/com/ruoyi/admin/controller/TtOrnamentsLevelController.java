package com.ruoyi.admin.controller;

import com.ruoyi.domain.other.TtOrnamentsLevel;
import com.ruoyi.admin.service.TtOrnamentsLevelService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "管理端 饰品等级信息")
@RestController
@RequestMapping("/admin/ornamentsLevel")
public class TtOrnamentsLevelController extends BaseController {

    private final TtOrnamentsLevelService ornamentsLevelService;

    public TtOrnamentsLevelController(TtOrnamentsLevelService ornamentsLevelService) {
        this.ornamentsLevelService = ornamentsLevelService;
    }

    @ApiOperation("获取所有饰品等级背景")
    @GetMapping("/list")
    public PageDataInfo<TtOrnamentsLevel> list() {
        startPage();
        List<TtOrnamentsLevel> list = ornamentsLevelService.list();
        return getPageData(list);
    }

    @GetMapping(value = "/{id}")
    public R<TtOrnamentsLevel> getInfo(@PathVariable("id") Integer id) {
        TtOrnamentsLevel ornamentsLevel = ornamentsLevelService.getById(id);
//        ornamentsLevel.setLevelImg("");
        return R.ok(ornamentsLevel);
    }

    @PostMapping("generateOrnamentsLevel/{num}")
    public AjaxResult generateOrnamentsLevel(@PathVariable("num") Integer num) {
        String msg = ornamentsLevelService.generateOrnamentsLevel(num);
        return StringUtils.isEmpty(msg) ? AjaxResult.success("生成成功，请修改参数！") : AjaxResult.error(msg);
    }

    @PutMapping
    public AjaxResult edit(@RequestBody TtOrnamentsLevel ttOrnamentsLevel) {
        String msg = ornamentsLevelService.updateOrnamentsLevelById(ttOrnamentsLevel);
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }

    @PostMapping
    public AjaxResult truncateOrnamentsLevel() {
        ornamentsLevelService.truncateOrnamentsLevel();
        return AjaxResult.success("重置成功，请重新设置！");
    }
}
