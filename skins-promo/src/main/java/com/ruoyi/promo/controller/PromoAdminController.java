package com.ruoyi.promo.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.domain.entity.TtCommissionRecord;
import com.ruoyi.promo.service.CommissionRecordService;
import com.ruoyi.promo.service.PromoTurnoverService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Api(tags = "佣金管理-管理端")
@RestController
@RequestMapping("/admin/commission")
public class PromoAdminController extends BaseController {

    @Autowired
    private PromoTurnoverService promoTurnoverService;

    @Autowired
    private CommissionRecordService commissionRecordService;

    @ApiOperation("获取某个用户的佣金列表")
    @GetMapping("/getCommissionListByUserId")
    public AjaxResult getCommissionListByUserId(@RequestParam("userId") Integer userId) {
        List<TtCommissionRecord> commissionList = promoTurnoverService.getCommissionList(userId);
        return AjaxResult.success(commissionList);
    }

    @ApiOperation("发放某个主播的佣金")
    @GetMapping("/getCommissionById")
    public AjaxResult getCommissionById(@RequestParam("commissionId") Integer commissionId) {

        return commissionRecordService.changeCommissionRecord(commissionId, "运营管理发放");

    }


}
