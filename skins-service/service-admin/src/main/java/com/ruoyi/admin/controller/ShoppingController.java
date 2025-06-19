package com.ruoyi.admin.controller;

import com.ruoyi.admin.service.TtOrnamentService;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.admin.service.ShoppingService;
import com.ruoyi.domain.other.ShoppingBody;
import com.ruoyi.domain.vo.ShoppingDataVO;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "管理端 商城管理")
@RestController
@RequestMapping("/admin/shopping")
public class ShoppingController extends BaseController {

    private final ShoppingService shoppingService;
    private final TtOrnamentService ornamentsService;

    public ShoppingController(ShoppingService shoppingService,
                              TtOrnamentService ornamentsService) {
        this.shoppingService = shoppingService;
        this.ornamentsService = ornamentsService;
    }

    @ApiOperation("商品列表")
    @GetMapping("/list")
    public PageDataInfo<ShoppingDataVO> list(ShoppingBody shoppingBody) {
        return shoppingService.list(shoppingBody);
    }

    @PostMapping("/batchPutAwayOrSoldOut/{status}")
    public R<Boolean> batchPutAwayOrSoldOut(@RequestBody List<TtOrnament> ornamentsList,
                                            @PathVariable("status") String status) {
        if (StringUtils.isNull(ornamentsList) || ornamentsList.isEmpty()) return R.fail();
        ornamentsList = ornamentsList.stream().peek(ttOrnaments -> ttOrnaments.setIsPutaway(status)).collect(Collectors.toList());
        if (ornamentsService.updateBatchById(ornamentsList, 1)) return R.ok(true);
        return R.fail(false);
    }
}
