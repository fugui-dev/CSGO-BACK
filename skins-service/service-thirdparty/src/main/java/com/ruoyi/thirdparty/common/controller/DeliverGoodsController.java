package com.ruoyi.thirdparty.common.controller;

import com.ruoyi.domain.dto.deliver.TradeBuyParam;
import com.ruoyi.domain.vo.AvailableMarketOrnamentVO;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.thirdparty.common.service.DeliverGoodsService;
import com.ruoyi.thirdparty.zbt.param.ProductListParams;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "管理端发货管理")
@RestController
@RequestMapping("/admin/deliverGoods")
public class DeliverGoodsController extends BaseController {

    private final DeliverGoodsService deliverGoodsService;

    public DeliverGoodsController(DeliverGoodsService deliverGoodsService) {
        this.deliverGoodsService = deliverGoodsService;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GetAvailableMarketListParam{
        // private List<String> hashNameList;
        private Integer partyType;
        private List<Long> ornamentsId;
    }

    // 获取饰品在各个平台的在售信息
    @ApiOperation("获取饰品在各个平台的在售信息")
    @PostMapping("/getAvailableMarketList")
    public R getAvailableMarketList(@RequestBody GetAvailableMarketListParam param) {
        return deliverGoodsService.getAvailableMarketList(param);
    }

    // 根据饰品hashName获取所有平台的在售列表（弃用接口）
    @ApiOperation("根据饰品hashName获取所有平台的在售列表（弃用接口）")
    @GetMapping("/getAvailableMarketListByHashName/{marketHashName}")
    public R<List<AvailableMarketOrnamentVO>> getAvailableMarketListByHashName(@PathVariable("marketHashName") String marketHashName,
                                                                               ProductListParams productListParams) {
        List<AvailableMarketOrnamentVO> list = deliverGoodsService.getAvailableMarketListByHashName(marketHashName, productListParams);
        if (StringUtils.isNull(list)) return R.fail("获取饰品所有在售列表异常！");
        return R.ok(list);
    }

    @ApiOperation("购买发货")
    @PostMapping("/tradeBuy")
    public R tradeBuy(@RequestBody TradeBuyParam param) {
        return deliverGoodsService.tradeBuy(param);
    }

    @ApiOperation("同步状态")
    @GetMapping("/synchronousStatus")
    public R<Boolean> synchronousStatus(String outTradeNo) {
        String msg = deliverGoodsService.synchronousStatus(outTradeNo);
        return StringUtils.isEmpty(msg) ? R.ok(true, "同步成功！") : R.fail(false, msg);
    }
}
