package com.ruoyi.user.controller;

import cn.hutool.core.util.ObjectUtil;
import com.ruoyi.admin.service.TtOrderService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Api(tags = "订单信息")
@RestController
@RequestMapping("/api/order")
public class ApiOrderController extends BaseController {

    @Autowired
    private TtOrderService ttOrderService;

    @ApiOperation("充值明细")
    @GetMapping("/list")
    public R list(Integer page, Integer size) {
        Long userId = getUserId();
        if (ObjectUtil.isNull(userId)) return R.fail("登录过期。");
        return ttOrderService.clientList(page,size,userId.intValue());
    }

}
