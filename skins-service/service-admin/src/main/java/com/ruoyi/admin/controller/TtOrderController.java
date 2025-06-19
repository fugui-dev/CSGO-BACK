package com.ruoyi.admin.controller;

import com.ruoyi.admin.service.TtOrderService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.domain.dto.sys.OrderQueryCondition;
import com.ruoyi.domain.entity.TtOrder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "管理端 订单管理")
@RestController
@RequestMapping("/admin/order")
public class TtOrderController extends BaseController {

    private final TtOrderService ttOrderService;

    public TtOrderController(TtOrderService ttOrderService) {
        this.ttOrderService = ttOrderService;
    }

    @ApiOperation("网站支付订单查询")
    @PostMapping("/list")
    public R list(@RequestBody @Validated OrderQueryCondition condition) {
        return ttOrderService.adminList(condition);
    }

}
