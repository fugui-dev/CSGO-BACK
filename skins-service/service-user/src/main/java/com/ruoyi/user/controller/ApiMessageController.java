package com.ruoyi.user.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.user.service.ApiMessageService;
import com.ruoyi.domain.vo.ApiMessageDataVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Api(tags = "消息")
@RestController
@RequestMapping("/api/message")
public class ApiMessageController extends BaseController {

    private final ApiMessageService apiMessageService;

    public ApiMessageController(ApiMessageService apiMessageService) {
        this.apiMessageService = apiMessageService;
    }

    @ApiOperation("获取消息列表")
    @GetMapping("/getMessageList")
    public PageDataInfo<ApiMessageDataVO> getMessageList(@RequestParam(required = false) Integer id){
        startPage();
        List<ApiMessageDataVO> list = apiMessageService.getMessageList(getUserId(), id);
        return getPageData(list);
    }

    @ApiOperation("已读")
    @GetMapping("/view")
    public R<ApiMessageDataVO> view(Integer id){
        ApiMessageDataVO data = apiMessageService.view(getUserId(), id);
        return R.ok(data);
    }

    @ApiOperation("分批操作")
    @PostMapping("/batchOperation")
    public R<Boolean> batchOperation(@RequestParam Integer[] ids, @RequestParam("status") String status){
        String msg = apiMessageService.batchOperation(getUserId(), Arrays.asList(ids), status);
        return StringUtils.isEmpty(msg) ? R.ok(true, "操作成功！") : R.fail(false, msg);
    }

}
