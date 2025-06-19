package com.ruoyi.playingmethod.controller;

import com.ruoyi.admin.service.TtUserAmountRecordsService;
import com.ruoyi.admin.service.TtBoxService;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.UpdateUserCache;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.dto.boxRecords.queryCondition;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.other.*;
import com.ruoyi.domain.vo.OpenBoxVO;
import com.ruoyi.domain.vo.boxRecords.TtBoxRecordsVO;
import com.ruoyi.playingmethod.entity.request.OpenBox2Request;
import com.ruoyi.playingmethod.service.ApiBindBoxService;
import com.ruoyi.playingmethod.service.ApiBoxRecordsService;
import com.ruoyi.system.service.ISysConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Api(tags = "玩转盲盒模式")
@RestController
@RequestMapping("/api/bindbox")
public class ApiBindBoxController extends BaseController {

    private final ISysConfigService sysConfigService;
    private final ApiBindBoxService bindBoxService;
    private final TtUserService userService;
    private final TtBoxService boxService;
    private final TtUserAmountRecordsService userAmountRecordsService;

    public ApiBindBoxController(ISysConfigService sysConfigService,
                                ApiBindBoxService bindBoxService,
                                TtUserAmountRecordsService userAmountRecordsService,
                                TtUserService userService,
                                TtBoxService boxService) {
        this.sysConfigService = sysConfigService;
        this.bindBoxService = bindBoxService;
        this.userService = userService;
        this.userAmountRecordsService = userAmountRecordsService;
        this.boxService = boxService;
    }

    @Autowired
    private ApiBoxRecordsService apiBoxRecordsService;

    @ApiOperation("获取宝箱信息")
    @Anonymous
    @GetMapping("/{boxId}")
    public R<TtBoxA> getBoxData(@PathVariable(value = "boxId") Integer boxId) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        String bindBoxMaintenance = sysConfigService.selectConfigByKey("bindBoxMaintenance");
        if ("1".equals(bindBoxMaintenance)) {
            return R.fail("盲盒开箱功能正在维护中......");
        }
        TtBoxA boxData = bindBoxService.getBoxData(boxId);
        return R.ok(boxData);
    }

    @ApiOperation("获取宝箱列表")
    @Anonymous
    @GetMapping("/getBoxList")
    public R<List<TtBoxVO>> getBoxList(@RequestParam(value = "boxTypeId", required = false) Integer boxTypeId,
                                       @RequestParam(value = "homeFlag", required = false) String homeFlag,
                                       @RequestParam(value = "isFight", required = false) Integer isFight) {
        isFight = null; //隐藏是否对战宝箱数据
        List<TtBoxVO> boxData = bindBoxService.getBoxList(boxTypeId, homeFlag, isFight);
        if (boxData.isEmpty()) {
            return R.ok(null, "没有匹配的数据。");
        }
        return R.ok(boxData);
    }

    /**
     * 盲盒开箱模式开箱-注入饰品模式开箱
     */
    @ApiOperation("盲盒开箱-注入饰品开箱（会同时消耗第二套货币）")
    @UpdateUserCache
    @PostMapping("/openBox2")
    public R<List<OpenBoxVO>> openBox2(@RequestBody @Validated OpenBox2Request openBox2Request) {

        if (openBox2Request.getNum() < 0){
            return R.fail("非法参数！");
        }
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        String bindBoxMaintenance = sysConfigService.selectConfigByKey("bindBoxMaintenance");
        if ("1".equals(bindBoxMaintenance)) {
            return R.fail("盲盒开箱功能正在维护中......");
        }
        TtUser ttUser = userService.getById(getUserId());
//        TtBox ttBox = boxService.getById(openBox2Request.getBoxId());
        if (BigDecimal.ZERO.compareTo(ttUser.getTotalRecharge()) >= 0){
            return R.fail("为防止机器人恶意注册，请充值后开启玩法！");
        }


//        return bindBoxService.blindBox(ttUser, ttBox, openBox2Request.getNum());
        return bindBoxService.openBox2(openBox2Request);
    }

    /**
     * 盲盒开箱模式开箱
     */
    @ApiOperation("盲盒开箱")
    @UpdateUserCache
    @PostMapping("/openBox")
    public R<List<OpenBoxVO>> openBox(@RequestParam("boxId") Integer boxId, @RequestParam("num") Integer num) {
        if (num < 0){
            return R.fail("非法参数！");
        }
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        String bindBoxMaintenance = sysConfigService.selectConfigByKey("bindBoxMaintenance");
        if ("1".equals(bindBoxMaintenance)) {
            return R.fail("盲盒开箱功能正在维护中......");
        }
        TtUser ttUser = userService.getById(getUserId());
        TtBox ttBox = boxService.getById(boxId);
        if (BigDecimal.ZERO.compareTo(ttUser.getTotalRecharge()) >= 0){
            return R.fail("为防止机器人恶意注册，请充值后开启玩法！");
        }
//        if ("0".equals(ttUser.getIsRealCheck())) {
//            return R.fail("您的账户未进行实名认证，请先进行实名认证！");
//        }
         BigDecimal openBoxBeanTotal = ttBox.getPrice().multiply(new BigDecimal(num));
         if (ttUser.getAccountAmount().compareTo(openBoxBeanTotal) < 0) return R.fail("您的账户游戏币不足！");
        return bindBoxService.blindBox(ttUser, ttBox, num);
    }

    @ApiOperation("获取历史开箱信息")
    @Anonymous
    @PostMapping("/getBindBoxHistory")
    public R getBindBoxHistory(@RequestBody @Validated queryCondition param) {
        if (param.getStatus().equals(1) || param.getStatus().equals(10)) {
            return R.fail("非法的状态参数。");
        }
        List<TtBoxRecordsVO> ttBoxRecordsVOS = apiBoxRecordsService.byCondition(param);
        return R.ok(ttBoxRecordsVOS);
    }

    // @ApiOperation("获取历史个人开箱信息")
    // @Anonymous
    // @GetMapping("/selectBoxRecordsUserLogList")
    // public R selectBoxRecordsUserLogList(TtUserAmountRecordsBody ttUserAmountRecordsBody) {
    //     System.out.println("==================个人开箱记录====================");
    //     ttUserAmountRecordsBody.setUserId(getUserId().intValue());
    //     return userAmountRecordsService.queryList(ttUserAmountRecordsBody);
    //     //return R.ok(list);
    // }
}
