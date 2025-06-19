package com.ruoyi.admin.controller;

import com.ruoyi.domain.entity.fight.TtFight;
import com.ruoyi.admin.service.TtFightService;
import com.ruoyi.domain.vo.FightBoxDataVO;
import com.ruoyi.domain.other.TtFightBody;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.PageDataInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/fight")
public class TtFightController extends BaseController {

    private final TtFightService fightService;

    public TtFightController(TtFightService fightService) {
        this.fightService = fightService;
    }

    @GetMapping("/list")
    public PageDataInfo<TtFight> list(TtFightBody ttFightBody) {
        startPage();
        List<TtFight> list = fightService.selectFightList(ttFightBody);
        return getPageData(list);
    }

    @GetMapping("/getFightBoxList/{fightId}")
    public PageDataInfo<FightBoxDataVO> getFightBoxList(@PathVariable("fightId") Integer fightId) {
        startPage();
        List<FightBoxDataVO> list = fightService.selectFightBoxList(fightId);
        return getPageData(list);
    }
}
