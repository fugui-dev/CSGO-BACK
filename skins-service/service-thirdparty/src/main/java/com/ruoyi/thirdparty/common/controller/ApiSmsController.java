package com.ruoyi.thirdparty.common.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.thirdparty.common.model.ApiSmsBody;
import com.ruoyi.thirdparty.common.service.ApiSmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "短信")
@RestController
@RequestMapping("/api/sms")
public class ApiSmsController extends BaseController {

    private final ISysConfigService sysConfigService;
    private final ApiSmsService apiSmsService;

    public ApiSmsController(ISysConfigService sysConfigService,
                            ApiSmsService apiSmsService) {
        this.sysConfigService = sysConfigService;
        this.apiSmsService = apiSmsService;
    }

    @ApiOperation("获取短信验证码")
    @Anonymous
    @PostMapping("/getVerifyCode")
    public R<Object> getVerifyCode(@RequestBody ApiSmsBody smsBody) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        String msg = apiSmsService.getVerifyCode(smsBody);
        return StringUtils.isEmpty(msg) ? R.ok("获取验证码成功，请注意短信通知！") : R.fail(msg);
    }
}
