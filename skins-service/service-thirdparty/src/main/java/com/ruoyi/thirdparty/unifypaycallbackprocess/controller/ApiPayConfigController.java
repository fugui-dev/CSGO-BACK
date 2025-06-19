package com.ruoyi.thirdparty.unifypaycallbackprocess.controller;

import com.ruoyi.admin.mapper.TtUserMapper;
import com.ruoyi.admin.service.ITtPayConfigService;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.domain.entity.TtPayConfig;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.thirdparty.unifypaycallbackprocess.vo.TopUpRatioVO;
import io.jsonwebtoken.lang.Assert;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;


@Api(tags = "支付配置列表")
@RestController
@RequestMapping("/api/payConfig")
public class ApiPayConfigController extends BaseController {

    @Autowired
    private ITtPayConfigService ttPayConfigService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private TtUserService userService;

    @ApiOperation("查询支付配置列表")
    @GetMapping("/list")
    public R<List<TtPayConfig>> list()
    {
        Long userId = getUserId();
        TtUser user = userService.getById(userId);
        Assert.notNull(user, "用户信息异常，请重新登录！");

        TtPayConfig payConfig = new TtPayConfig();
        payConfig.setUserTotalMinMoney(user.getTotalRecharge().compareTo(BigDecimal.ZERO) >= 0 ? user.getTotalRecharge() : BigDecimal.ZERO);

//        startPage();
        List<TtPayConfig> list = ttPayConfigService.selectTtPayConfigList(payConfig);
        return R.ok(list);
    }

    @ApiOperation("查询充值返利比例和门槛")
    @GetMapping("/topUpRatio")
    public R<TopUpRatioVO> topUpRatio()
    {
        String rechargePointsRebateRatio = configService.selectConfigByKey("expenditure.points.rebate.ratio");
        String rechargeThreshold = configService.selectConfigByKey("expenditure.points.rebate.threshold");

        if (StringUtils.isBlank(rechargeThreshold) || StringUtils.isBlank(rechargePointsRebateRatio)){
            return R.ok(new TopUpRatioVO());
        }

        BigDecimal ratio = new BigDecimal(rechargePointsRebateRatio); //比例
        BigDecimal threshold = new BigDecimal(rechargeThreshold); //门槛
        return R.ok(new TopUpRatioVO(ratio, threshold));
    }

}
