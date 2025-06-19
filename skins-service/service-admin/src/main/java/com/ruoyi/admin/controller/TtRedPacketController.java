package com.ruoyi.admin.controller;

import com.ruoyi.admin.service.TtRedPacketService;
import com.ruoyi.domain.other.TtRedPacket;
import com.ruoyi.domain.other.TtRedPacketBody;
import com.ruoyi.domain.vo.TtRedPacketDataVO;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Api(tags = "管理端 红包")
@RestController
@RequestMapping("/admin/redPacket")
public class TtRedPacketController extends BaseController {

    private final TtRedPacketService redPacketService;

    public TtRedPacketController(TtRedPacketService redPacketService) {
        this.redPacketService = redPacketService;
    }

    @GetMapping("/list")
    public PageDataInfo<TtRedPacketDataVO> list(TtRedPacketBody ttRedPacketBody) {
        startPage();
        List<TtRedPacketDataVO> list = redPacketService.queryList(ttRedPacketBody);
        return getPageData(list);
    }

    @GetMapping(value = "/{id}")
    public R<TtRedPacketDataVO> getInfo(@PathVariable("id") Integer id) {
        TtRedPacketDataVO data = redPacketService.queryList(TtRedPacketBody.builder().id(id).build()).get(0);
        return R.ok(data);
    }

    @ApiOperation("生成红包")
    @PostMapping
    public AjaxResult add(@RequestBody TtRedPacketDataVO ttRedPacketDataVO) {
        List<String> passwordList = redPacketService.insertRedPacket(ttRedPacketDataVO);
        return StringUtils.isNotNull(passwordList) ? AjaxResult.success("生成成功！", passwordList) : AjaxResult.error("生成红包异常");
    }

    @PutMapping
    public AjaxResult edit(@RequestBody TtRedPacket redPacket) {
        redPacket.setUpdateBy(getUsername());
        redPacket.setUpdateTime(DateUtils.getNowDate());
        return redPacketService.updateRedPacketById(redPacket);
        // return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids) {

        boolean b = redPacketService.removeByIds(Arrays.asList(ids));

        if (b) return AjaxResult.success();
        return AjaxResult.error();
    }
}
