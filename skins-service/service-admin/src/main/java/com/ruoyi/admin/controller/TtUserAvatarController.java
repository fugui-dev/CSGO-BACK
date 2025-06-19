package com.ruoyi.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.domain.other.TtUserAvatar;
import com.ruoyi.admin.service.TtUserAvatarService;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/admin/userAvatar")
@Slf4j
public class TtUserAvatarController extends BaseController {

    private final TtUserAvatarService userAvatarService;

    public TtUserAvatarController(TtUserAvatarService userAvatarService) {
        this.userAvatarService = userAvatarService;
    }

    @GetMapping("/list")
    public PageDataInfo<TtUserAvatar> list() {
        startPage();
        List<TtUserAvatar> list = userAvatarService.list(Wrappers.lambdaQuery(TtUserAvatar.class)
                        .orderByAsc(TtUserAvatar::getSort));
        return getPageData(list);
    }

    @GetMapping(value = "/{id}")
    public R<TtUserAvatar> getInfo(@PathVariable("id") Integer id) {
        TtUserAvatar ttUserAvatar = userAvatarService.getById(id);
//        ttUserAvatar.setAvatar("");
        return R.ok(ttUserAvatar);
    }

    @PostMapping
    public AjaxResult add(@RequestBody TtUserAvatar ttUserAvatar) {
        if (StringUtils.isEmpty(ttUserAvatar.getAvatar())) ttUserAvatar.setAvatar("");
        else ttUserAvatar.setAvatar(RuoYiConfig.getDomainName() + ttUserAvatar.getAvatar());
        return toAjax(userAvatarService.save(ttUserAvatar));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody TtUserAvatar ttUserAvatar) {
        String msg = userAvatarService.updateUserAvatarById(ttUserAvatar);
        return StringUtils.isEmpty(msg) ? AjaxResult.success() : AjaxResult.error(msg);
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids) {
        return toAjax(userAvatarService.removeByIds(Arrays.asList(ids)));
    }
}
