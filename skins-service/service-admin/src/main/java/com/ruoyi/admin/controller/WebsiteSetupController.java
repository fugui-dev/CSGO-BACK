package com.ruoyi.admin.controller;

import com.ruoyi.admin.service.WebsiteSetupService;
import com.ruoyi.domain.other.OperationalStatistics;
import com.ruoyi.domain.other.ParameterSettingBody;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/websiteSetup")
public class WebsiteSetupController extends BaseController {

    private final WebsiteSetupService websiteSetupService;

    public WebsiteSetupController(WebsiteSetupService websiteSetupService) {
        this.websiteSetupService = websiteSetupService;
    }

    @GetMapping("getOperationalStatistics")
    public R<List<OperationalStatistics>> getOperationalStatistics() {
        List<OperationalStatistics> list = websiteSetupService.getOperationalStatistics();
        return R.ok(list);
    }

    @PostMapping("/getParameterSetting")
    public R<ParameterSettingBody> getParameterSetting() {
        return R.ok(websiteSetupService.getParameterSetting());
    }

    @PostMapping("/updateParameterSetting")
    public AjaxResult updateParameterSetting(@RequestBody ParameterSettingBody parameterSettingBody) {
        String msg = websiteSetupService.updateParameterSetting(parameterSettingBody);
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }
}
