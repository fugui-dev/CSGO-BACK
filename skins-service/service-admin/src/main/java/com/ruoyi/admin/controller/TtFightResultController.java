package com.ruoyi.admin.controller;

import com.ruoyi.admin.service.TtFightResultService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.domain.vo.FightResultDataVO;
import com.ruoyi.domain.vo.fight.FightResultVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/fightResult")
public class TtFightResultController {

    private final TtFightResultService fightResultService;

    public TtFightResultController(TtFightResultService fightResultService) {
        this.fightResultService = fightResultService;
    }

    // @GetMapping("/getFightResult/{fightId}")
    // public R<FightResultDataVO> getFightResult(@PathVariable("fightId") Integer fightId) {
    //     FightResultDataVO fightResultDataVO = fightResultService.getFightResult(fightId);
    //     return R.ok(fightResultDataVO);
    // }

    @GetMapping("/getFightResult/{fightId}")
    public R<FightResultVO> getFightResult(@PathVariable("fightId") Integer fightId) {
        FightResultVO fightResultVO = fightResultService.getFightResult(fightId);
        return R.ok(fightResultVO);
    }
}
