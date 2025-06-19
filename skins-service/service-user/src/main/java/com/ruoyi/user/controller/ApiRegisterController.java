package com.ruoyi.user.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.user.service.ApiRegisterService;
import com.ruoyi.domain.other.ApiRegisterBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "用户注册")
@RestController
@RequestMapping("/api/register")
public class ApiRegisterController extends BaseController {

    private final ISysConfigService sysConfigService;
    private final ApiRegisterService apiRegisterService;

    public ApiRegisterController(ISysConfigService sysConfigService,
                                 ApiRegisterService apiRegisterService) {
        this.sysConfigService = sysConfigService;
        this.apiRegisterService = apiRegisterService;
    }

    @ApiOperation("用户注册")
    @PostMapping
    @Anonymous
    public R<Object> register(@RequestBody @Validated ApiRegisterBody registerBody) {
        String websiteMaintenance = sysConfigService.selectConfigByKey("websiteMaintenance");
        if ("1".equals(websiteMaintenance)) {
            return R.fail("网站维护中......");
        }
        String msg = apiRegisterService.register(registerBody);
        return StringUtils.isEmpty(msg) ? R.ok("注册成功！") : R.fail(msg);
    }
}
