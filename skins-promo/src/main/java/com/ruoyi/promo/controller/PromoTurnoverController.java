package com.ruoyi.promo.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.domain.entity.TtCommissionRecord;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.promo.domain.vo.AnchorDayTurnoverVO;
import com.ruoyi.promo.service.CommissionRecordService;
import com.ruoyi.promo.service.PromoTurnoverService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Api(tags = "推广流水")
@RestController
@RequestMapping("/promo/turnover")
public class PromoTurnoverController extends BaseController {

    @Autowired
    private PromoTurnoverService promoTurnoverService;

    @Autowired
    private CommissionRecordService commissionRecordService;

    @ApiOperation("获取实时数据")
    @GetMapping("/getRealTimeData")
    public AjaxResult getRealTimeData() {
        Long userId = SecurityUtils.getUserId();
        return promoTurnoverService.getRealTimeData(userId);
    }

    @ApiOperation("获取近10天推广数据")
    @GetMapping("getLast10DaysPromotionData")
    public AjaxResult getLast10DaysPromotionData() {
        Long userId = SecurityUtils.getUserId();
        return promoTurnoverService.getLast10DaysPromotionData(userId);
    }

    @ApiOperation("获取主播推广数据")
    @GetMapping("/getAnchorPromotionData")
    public TableDataInfo getAnchorPromotionData(AnchorDayTurnoverVO anchorDayTurnoverVO) {
        startPage();
        Long userId = SecurityUtils.getUserId();
        anchorDayTurnoverVO.setAnchorId(userId);
        List<AnchorDayTurnoverVO> anchorDayTurnoverVOList = promoTurnoverService.getAnchorDayTurnover(anchorDayTurnoverVO);
        return getDataTable(anchorDayTurnoverVOList);
    }

    @ApiOperation("获取下级分支")
    @GetMapping("/getSubBranches")
    public AjaxResult getSubBranches() {
        Long userId = getUserId();
        List<TtUser> ttUserList = promoTurnoverService.getSubBranches(userId);
        return AjaxResult.success(ttUserList);
    }

    @ApiOperation("获取消费记录")
    @GetMapping("/getPurchaseByUserId/{userId}")
    public AjaxResult getPurchaseByUserId(@PathVariable("userId") Integer userId) {
        return AjaxResult.success(promoTurnoverService.getPurchaseByUserId(userId));
    }

    @ApiOperation("获取佣金比例")
    @GetMapping("/getCommissionRateByUserId/{userId}")
    public AjaxResult getCommissionRateByUserId(@PathVariable("userId") Integer userId) {
        BigDecimal commissionRate = promoTurnoverService.getCommissionRateByUserId(userId);
        Map<String, Object> map = new HashMap<>();
        map.put("commissionRate", commissionRate);
        return AjaxResult.success(map);
    }

    @ApiOperation("获取佣金比例")
    @PutMapping("/updateCommissionRate")
    public AjaxResult updateCommissionRate(@RequestBody TtUser ttUser) {
        Integer userId = ttUser.getUserId();
        BigDecimal commissionRate = ttUser.getCommissionRate();
        if (Objects.isNull(userId)) {
            return AjaxResult.error("用户ID不能为空");
        }
        if (Objects.isNull(commissionRate)) {
            return AjaxResult.error("分佣比例不能为空");
        }
        if (commissionRate.compareTo(BigDecimal.valueOf(0.1)) > 0) {
            return AjaxResult.error("分佣比例不能超过0.1");
        }
        if (commissionRate.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            return AjaxResult.error("分佣比例不能低于0.01");
        }
        int row = promoTurnoverService.updateCommissionRate(userId, commissionRate);
        return row >= 1 ? AjaxResult.success("修改成功") : AjaxResult.error("修改失败");
    }

    @ApiOperation("获取佣金列表")
    @GetMapping("/getCommissionList")
    public AjaxResult getCommissionList() {
        Long userId = getUserId();
        List<TtCommissionRecord> commissionList = promoTurnoverService.getCommissionList(userId.intValue());
        return AjaxResult.success(commissionList);
    }

    @ApiOperation("领取佣金")
    @GetMapping("/getCommissionById")
    public AjaxResult getCommissionById(@RequestParam("commissionId") Integer commissionId) {

        return commissionRecordService.changeCommissionRecord(commissionId, "主播领取！");

    }

}
