package com.ruoyi.playingmethod.controller;

import com.ruoyi.admin.service.TtUserAvatarService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.other.TtUserAvatar;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "用户头像")
@RestController
@RequestMapping("/api/userAvatar")
public class ApiUserAvatarController extends BaseController {

    private final TtUserAvatarService userAvatarService;

    public ApiUserAvatarController(TtUserAvatarService userAvatarService) {
        this.userAvatarService = userAvatarService;
    }

    @ApiOperation("用户头像列表")
    @GetMapping("/list")
    public R<List<TtUserAvatar>> list() {
        List<TtUserAvatar> list = userAvatarService.list();
        return R.ok(list);
    }
}
