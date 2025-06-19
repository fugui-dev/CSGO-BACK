package com.ruoyi.thirdparty.common.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.thirdparty.common.service.ApiCommonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Api(tags = "第三方公共接口")
@RestController
@RequestMapping("/api/thirdparty/common")
public class ApiCommonController {

    private final ApiCommonService commonService;

    public ApiCommonController(ApiCommonService commonService) {
        this.commonService = commonService;
    }

    @ApiOperation("生成二维码")
    @PostMapping("/skipLinkToQRCode")
    public R<Object> skipLinkToQRCode(HttpServletResponse response, @RequestParam("contents") String contents) throws IOException {
        return commonService.writerPayImage(response, contents);
    }
}
