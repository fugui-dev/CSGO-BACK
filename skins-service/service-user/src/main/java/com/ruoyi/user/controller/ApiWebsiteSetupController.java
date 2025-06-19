package com.ruoyi.user.controller;

import com.ruoyi.domain.other.TtBanner;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.user.service.ApiWebsiteSetupService;
import com.ruoyi.domain.vo.ApiContentDataVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "网站设置")
@RestController
@RequestMapping("/api/websiteSetup")
public class ApiWebsiteSetupController {

    private final ApiWebsiteSetupService apiWebsiteSetupService;

    public ApiWebsiteSetupController(ApiWebsiteSetupService apiWebsiteSetupService) {
        this.apiWebsiteSetupService = apiWebsiteSetupService;
    }

    @ApiOperation("获取横幅列表")
    @Anonymous
    @GetMapping("/getBannerList")
    public R<List<TtBanner>> getBannerList() {
        List<TtBanner> list = apiWebsiteSetupService.getBannerList();
        return R.ok(list);
    }

    @ApiOperation("获取类型内容")
    @GetMapping("/getContentByType")
    @Anonymous
    public R<ApiContentDataVO> getContentByType(String alias) {
        ApiContentDataVO data = apiWebsiteSetupService.getContentByType(alias);
        return R.ok(data);
    }
}
