package com.ruoyi.admin.controller;

import com.ruoyi.domain.other.TtMessage;
import com.ruoyi.admin.service.TtMessageService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/admin/message")
public class TtMessageController extends BaseController {

    private final TtMessageService messageService;

    public TtMessageController(TtMessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/list")
    public PageDataInfo<TtMessage> list(TtMessage ttMessage) {
        startPage();
        List<TtMessage> list = messageService.queryList(ttMessage);
        return getPageData(list);
    }

    @GetMapping(value = "/{id}")
    public R<TtMessage> getInfo(@PathVariable("id") Integer id) {
        return R.ok(messageService.getById(id));
    }

    @PostMapping
    public AjaxResult add(@RequestBody TtMessage ttMessage) {
        ttMessage.setCreateTime(DateUtils.getNowDate());
        return toAjax(messageService.save(ttMessage));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody TtMessage ttMessage) {
        ttMessage.setUpdateTime(DateUtils.getNowDate());
        return toAjax(messageService.updateById(ttMessage));
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids) {
        String msg = messageService.delByIds(Arrays.asList(ids));
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }

    @PostMapping("/singleMessage/{messageId}/{userIds}")
    public AjaxResult singleMessage(@PathVariable("messageId") Integer messageId, @PathVariable Integer[] userIds){
        String msg = messageService.singleMessage(messageId, Arrays.asList(userIds));
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }

    @PostMapping("/massMessaging/{messageId}")
    public AjaxResult massMessaging(@PathVariable("messageId") Integer messageId){
        String msg = messageService.massMessaging(messageId);
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }
}
