package com.ruoyi.admin.controller;

import com.ruoyi.admin.service.TtDeliveryRecordService;
import com.ruoyi.domain.other.TtDeliveryApplyBody;
import com.ruoyi.domain.vo.DeliveryApplyVO;
import com.ruoyi.domain.other.TtDeliveryRecordBody;
import com.ruoyi.domain.vo.TtDeliveryRecordDataVO;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "管理端 发货管理")
@RestController
@RequestMapping("/admin/deliverGoods")
public class TtDeliveryRecordController extends BaseController {

    private final TtDeliveryRecordService deliveryRecordService;

    public TtDeliveryRecordController(TtDeliveryRecordService deliveryRecordService) {
        this.deliveryRecordService = deliveryRecordService;
    }

    @ApiOperation("发货申请列表")
    @GetMapping("/getDeliveryApplyList")
    public PageDataInfo<DeliveryApplyVO> getDeliveryApplyList(TtDeliveryApplyBody deliveryApplyBody) {
        startPage();
        List<DeliveryApplyVO> list = deliveryRecordService.getDeliveryApplyList(deliveryApplyBody);
        return getPageData(list);
    }

    @ApiOperation("退回发货申请")
    @PostMapping("/deliveryFail")
    public R<Boolean> deliveryFail(@RequestParam("deliveryRecordId") Integer deliveryRecordId, @RequestParam("message") String message) {
        String msg = deliveryRecordService.deliveryFail(deliveryRecordId, message);
        return StringUtils.isEmpty(msg) ? R.ok(true, "操作成功！") : R.fail(false, msg);
    }

    @ApiOperation("发货记录申请")
    @GetMapping("/getDeliveryRecordList")
    public PageDataInfo<TtDeliveryRecordDataVO> getDeliveryRecordList(TtDeliveryRecordBody deliveryRecordBody) {
        startPage();
        List<TtDeliveryRecordDataVO> list = deliveryRecordService.getDeliveryRecordList(deliveryRecordBody);
        return getPageData(list);
    }

}
