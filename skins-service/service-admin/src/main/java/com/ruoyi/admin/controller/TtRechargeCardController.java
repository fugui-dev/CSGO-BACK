package com.ruoyi.admin.controller;

import com.ruoyi.domain.other.TtRechargeCard;
import com.ruoyi.admin.service.TtRechargeCardService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/admin/rechargeCard")
public class TtRechargeCardController extends BaseController {

    private final TtRechargeCardService rechargeCardService;

    public TtRechargeCardController(TtRechargeCardService rechargeCardService) {
        this.rechargeCardService = rechargeCardService;
    }

    @GetMapping("/list")
    public PageDataInfo<TtRechargeCard> list(TtRechargeCard ttRechargeCard) {
        startPage();
        List<TtRechargeCard> list = rechargeCardService.queryList(ttRechargeCard);
        return getPageData(list);
    }

    @PostMapping("/export")
    public void export(HttpServletResponse response, TtRechargeCard ttRechargeCard) {
        rechargeCardService.export(response, ttRechargeCard);
    }

    @PostMapping("/generateCard/{rechargeListId}/{num}")
    public R<List<String>> generateCard(@PathVariable("rechargeListId") Integer rechargeListId, @PathVariable("num") Integer num) {
        List<String> cardList = rechargeCardService.generateCard(rechargeListId, num);
        return R.ok(cardList);
    }
}
