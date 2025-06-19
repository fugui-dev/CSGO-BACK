package com.ruoyi.admin.controller;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.ruoyi.admin.service.TtUpgradeFailOrnamentsService;
import com.ruoyi.domain.other.TtUpgradeFailOrnaments;
import com.ruoyi.domain.vo.TtUpgradeFailOrnamentsDataVO;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Api(tags = "管理端 失败物品奖励")
@RestController
@RequestMapping("/admin/upgradeFailOrnaments")
public class TtUpgradeFailOrnamentsController extends BaseController {

    private final TtUpgradeFailOrnamentsService upgradeFailOrnamentsService;

    public TtUpgradeFailOrnamentsController(TtUpgradeFailOrnamentsService upgradeFailOrnamentsService) {
        this.upgradeFailOrnamentsService = upgradeFailOrnamentsService;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class listParam {
        @NotNull(message = "参与升级物品id不能为空")
        @JsonSerialize(using = ToStringSerializer.class)
        private Long upgradeOrnamentId;
    }
    @ApiOperation("失败奖励列表")
    @PostMapping("/list")
    public PageDataInfo<TtUpgradeFailOrnamentsDataVO> list(@RequestBody listParam param) {
        startPage();
        List<TtUpgradeFailOrnamentsDataVO> list = upgradeFailOrnamentsService.queryList(param);
        return getPageData(list);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class BatchAddParam{
        @NotNull(message = "升级物品id不能为空")
        @JsonSerialize(using = ToStringSerializer.class)
        private Long upgradeId;
//        @NotEmpty(message = "失败物品列表不能为空")
        private Map<Long,Integer> ornamentsInfo;

        @NotEmpty(message = "失败物品列表不能为空")
        private List<Long> ornamentsIds;

        //是否应用所有
        private Boolean useAllFlag;
    }
    @ApiOperation("批量添加失败奖励")
    @PostMapping("/batchAdd")
    public AjaxResult batchAdd(@RequestBody @Validated BatchAddParam param) {
        return upgradeFailOrnamentsService.batchAdd(param);
    }

    @PutMapping
    public AjaxResult edit(@RequestBody TtUpgradeFailOrnamentsDataVO ttUpgradeFailOrnamentsDataVO) {
        String msg = upgradeFailOrnamentsService.updateUpgradeFailOrnamentsById(ttUpgradeFailOrnamentsDataVO);
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids) {
        return toAjax(upgradeFailOrnamentsService.removeByIds(Arrays.asList(ids)));
    }
}
