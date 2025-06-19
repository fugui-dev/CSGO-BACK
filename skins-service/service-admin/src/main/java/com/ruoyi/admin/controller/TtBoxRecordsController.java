package com.ruoyi.admin.controller;

import com.ruoyi.admin.service.TtBoxRecordsService;
import com.ruoyi.domain.other.TtBoxRecordsBody;
import com.ruoyi.domain.vo.TtBoxRecordsDataVO;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.PageDataInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/boxRecords")
public class TtBoxRecordsController extends BaseController {

    private final TtBoxRecordsService boxRecordsService;

    public TtBoxRecordsController(TtBoxRecordsService boxRecordsService) {
        this.boxRecordsService = boxRecordsService;
    }

    @GetMapping("/list")
    public PageDataInfo<TtBoxRecordsDataVO> list(TtBoxRecordsBody ttBoxRecordsBody) {
        startPage();
        List<TtBoxRecordsDataVO> list = boxRecordsService.selectBoxRecordsList(ttBoxRecordsBody);
        return getPageData(list);
    }
}
