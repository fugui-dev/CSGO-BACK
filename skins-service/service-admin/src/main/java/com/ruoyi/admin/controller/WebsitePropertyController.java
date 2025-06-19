package com.ruoyi.admin.controller;

import com.ruoyi.admin.service.TtOrnamentService;
import com.ruoyi.admin.service.WebsitePropertyService;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.domain.dto.queryCondition.OrnamentCondition;
import com.ruoyi.domain.vo.WebsitePropertyDataVO;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.vo.upgrade.SimpleOrnamentVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Api(tags = "管理端 道具管理")
@RestController
@RequestMapping("/admin/websiteProperty")
@Slf4j
public class WebsitePropertyController extends BaseController {

    private final WebsitePropertyService websitePropertyService;
    private final TtOrnamentService ornamentsService;

    public WebsitePropertyController(WebsitePropertyService websitePropertyService,
                                     TtOrnamentService ornamentsService) {
        this.websitePropertyService = websitePropertyService;
        this.ornamentsService = ornamentsService;
    }

    // @ApiOperation("获取道具列表")
    // @GetMapping("/list")
    // public PageDataInfo<WebsitePropertyDataVO> list() {
    //     startPage();
    //     List<WebsitePropertyDataVO> list = websitePropertyService.list();
    //     return getPageData(Arrays.asList());
    // }

    @ApiOperation("获取道具列表")
    @GetMapping("/list")
    public TableDataInfo list(OrnamentCondition condition) {
        startPage();
        List<SimpleOrnamentVO> list = ornamentsService.byCondition(condition);
        return getDataTable(list);
    }

    @GetMapping(value = "/{id}")
    public R<WebsitePropertyDataVO> getInfo(@PathVariable("id") Integer id) {
        WebsitePropertyDataVO websitePropertyDataVO = websitePropertyService.getById(id);
//        websitePropertyDataVO.setImageUrl("");
        return R.ok(websitePropertyDataVO);
    }

    @PostMapping
    public AjaxResult add(@RequestBody WebsitePropertyDataVO websitePropertyDataVO) {
        String msg = websitePropertyService.save(websitePropertyDataVO);
        return StringUtils.isEmpty(msg) ? AjaxResult.success("新增网站道具成功！") : AjaxResult.error(msg);
    }

    @PutMapping
    public AjaxResult edit(@RequestBody WebsitePropertyDataVO websitePropertyDataVO) {
        String msg = websitePropertyService.updateWebsitePropertyById(websitePropertyDataVO);
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids) {
        // return toAjax(ornamentsService.removeByIds(Arrays.asList(ids)));
        return toAjax(websitePropertyService.deleteWebsitePropertyByIds(ids));
    }
}
