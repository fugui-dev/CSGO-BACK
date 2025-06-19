package com.ruoyi.admin.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.domain.other.TtBoxOrnaments;
import com.ruoyi.admin.service.TtBoxOrnamentsService;
import com.ruoyi.domain.vo.TtBoxOrnamentsDataVO;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Api(tags = "管理端 宝箱物品")
@RestController
@RequestMapping("/admin/boxOrnaments")
public class TtBoxOrnamentsController extends BaseController {

    private final TtBoxOrnamentsService boxOrnamentsService;

    public TtBoxOrnamentsController(TtBoxOrnamentsService boxOrnamentsService) {
        this.boxOrnamentsService = boxOrnamentsService;
    }

    @ApiOperation("宝箱物品详情")
    @GetMapping("/list/{boxId}")
    public PageDataInfo<TtBoxOrnamentsDataVO> list(@PathVariable("boxId") Integer boxId) {
        startPage();
        List<TtBoxOrnamentsDataVO> list = boxOrnamentsService.selectTtBoxOrnamentsList(boxId);
        return getPageData(list);
    }

    @ApiOperation("宝箱统计数据")
    @GetMapping("/globalData/{boxId}")
    public R globalData(@PathVariable("boxId") Integer boxId) {
        return boxOrnamentsService.globalData(boxId);
    }

    @GetMapping(value = "/{id}")
    public R<TtBoxOrnaments> getInfo(@PathVariable("id") Integer id) {
        TtBoxOrnaments ttBoxOrnaments = boxOrnamentsService.getById(id);
        return R.ok(ttBoxOrnaments);
    }


//    @PreAuthorize("@ss.hasPermi('system:menu:edit')")
    @Log(title = "爆率设置", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('box:ornaments:add')")
    @PostMapping
    public AjaxResult add(@RequestBody TtBoxOrnaments ttBoxOrnaments) {
        ttBoxOrnaments.setCreateBy(getUsername());
        ttBoxOrnaments.setCreateTime(DateUtils.getNowDate());
        String msg = boxOrnamentsService.saveBoxOrnaments(ttBoxOrnaments);
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }

    /**
     * 修改宝箱物品
     *
     * @param ttBoxOrnamentsDataVO
     * @return
     */
    @PutMapping
    @PreAuthorize("@ss.hasPermi('box:ornaments:update')")
    @Log(title = "爆率修改", businessType = BusinessType.UPDATE)
    public AjaxResult edit(@RequestBody TtBoxOrnamentsDataVO ttBoxOrnamentsDataVO) {
        String msg = boxOrnamentsService.updateBoxOrnamentsById(ttBoxOrnamentsDataVO);
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }

    @DeleteMapping("/{boxId}/{ids}")
    public AjaxResult remove(@PathVariable Integer boxId, @PathVariable Long[] ids) {
        String msg = boxOrnamentsService.removeBoxOrnamentsByIds(boxId, Arrays.asList(ids));
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }

    @GetMapping("/getProfitMargin/{boxId}")
    public AjaxResult getProfitMargin(@PathVariable("boxId") Integer boxId) {
        return boxOrnamentsService.getProfitMargin(boxId);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class batchAddParam{
        private Integer boxId;
        private Integer partyType;
        // private List<Integer> ornamentsIds;

        @NotEmpty(message = "物品id不能为空")
        private List<Long> ornamentIds;
    }
    // 宝箱填货
    @ApiOperation("宝箱填货")
    @PostMapping("/batchAdd")
    public AjaxResult batchAdd(@RequestBody batchAddParam param) {
        return boxOrnamentsService.batchAdd(param);
        // return StringUtils.isEmpty(msg) ? AjaxResult.success("批量填货成功，请手动修改饰品数量！") : AjaxResult.error(msg);
    }

}
