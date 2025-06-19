package com.ruoyi.thirdparty.baiduPromotion.controller;

import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.entity.TbPromotionChannel;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.CreateOrderParam;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.thirdparty.baiduPromotion.BdPromotionProcess;
import com.ruoyi.thirdparty.baiduPromotion.LogidUrlEnum;
import com.ruoyi.thirdparty.jiujia.domain.CallbackBody;
import com.ruoyi.thirdparty.jiujia.service.JiuJiaPayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "百度推广")
@RestController
@RequestMapping("/api/bdPromotion")
public class BdPromotionController extends BaseController {

    @Autowired
    BdPromotionProcess bdPromotionProcess;

    @ApiOperation("联调接口")
    @PostMapping("/testApi")
    public R testApi(@RequestBody Map map) {
        //线索url
        String logidUrl = map.get("logidUrl").toString();

        //匹配域名，调用api
        TbPromotionChannel channel = bdPromotionProcess.baseProcess(logidUrl, LogidUrlEnum.REGISTER);

        if (channel == null) return R.fail("注册线索联调失败！");

        TbPromotionChannel channel1 = bdPromotionProcess.baseProcess(logidUrl, LogidUrlEnum.FIRST_RECHARGE);

        if (channel1 == null) return R.fail("首充线索联调失败！");

        return R.ok();

    }

}
