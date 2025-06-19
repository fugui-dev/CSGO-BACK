package com.ruoyi.admin.controller;

import com.ruoyi.domain.entity.recorde.TtUserCreditsRecords;
import com.ruoyi.admin.service.TtUserCreditsRecordsService;
import com.ruoyi.domain.other.TtUserCreditsRecordsBody;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.PageDataInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/userCreditsRecords")
public class TtUserCreditsRecordsController extends BaseController {

    private final TtUserCreditsRecordsService userCreditsRecordsService;

    public TtUserCreditsRecordsController(TtUserCreditsRecordsService userCreditsRecordsService) {
        this.userCreditsRecordsService = userCreditsRecordsService;
    }

    @GetMapping("/list")
    public PageDataInfo<TtUserCreditsRecords> list(TtUserCreditsRecordsBody ttUserCreditsRecordsBody) {
        startPage();
        List<TtUserCreditsRecords> list = userCreditsRecordsService.queryList(ttUserCreditsRecordsBody);
        return getPageData(list);
    }
}
