package com.ruoyi.admin.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.ruoyi.admin.service.TtOrnamentService;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.domain.other.TtOrnamentsBody;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.domain.vo.TtOrnamentVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(tags = "管理端 饰品管理")
@RestController
@RequestMapping("/admin/ornaments")
public class TtOrnamentsController extends BaseController {

    @Autowired
    private TtOrnamentService ornamentsService;

    @Autowired
    private TtOrnamentService ttOrnamentService;

    // 饰品列表多条件筛选
    @ApiOperation("饰品列表多条件筛选")
    @GetMapping("/list")
    public R list(TtOrnamentsBody param) {

        Page<TtOrnamentVO> ttOrnamentVOPage = ttOrnamentService.listByParam(param);

        return R.ok(ttOrnamentVOPage);
    }

    // TODO: 2024/3/29 !!!!!!
    // 导出饰品数据
    @Log(title = "饰品数据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TtOrnamentsBody ttOrnamentsBody) {
        List<TtOrnamentVO> list = ttOrnamentService.listByParam(ttOrnamentsBody).getRecords();
        ExcelUtil<TtOrnamentVO> util = new ExcelUtil<>(TtOrnamentVO.class);
        util.exportExcel(response, list, "饰品数据数据");
    }

    @GetMapping(value = "/{id}")
    public R<TtOrnament> getInfo(@PathVariable("id") Long id) {
        return R.ok(ornamentsService.getById(id));
    }

    @ApiOperation("饰品发放")
    @PostMapping("/grantOrnaments/{userId}/{ornamentsId}/{num}")
    public AjaxResult grantOrnaments(@PathVariable("userId") Integer userId,
                                     @PathVariable("ornamentsId") @JsonSerialize(using = ToStringSerializer.class) Long ornamentsId,
                                     @RequestParam(name = "ornamentsLevelId", required = false) Integer ornamentsLevelId,
                                     @PathVariable("num") Integer num) {
        return ornamentsService.grantOrnaments(userId, ornamentsId, ornamentsLevelId, num);
    }
}
