package com.ruoyi.admin.controller;

import com.ruoyi.domain.other.TtRechargeRecord;
import com.ruoyi.admin.service.TtRechargeRecordService;
import com.ruoyi.domain.other.TtRechargeRecordBody;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.poi.ExcelUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/admin/rechargeRecord")
public class TtRechargeRecordController extends BaseController {

    private final TtRechargeRecordService rechargeRecordService;

    public TtRechargeRecordController(TtRechargeRecordService rechargeRecordService) {
        this.rechargeRecordService = rechargeRecordService;
    }

    @GetMapping("/list")
    public PageDataInfo<TtRechargeRecord> list(TtRechargeRecordBody rechargeRecordBody) {
        startPage();
        List<TtRechargeRecord> list = rechargeRecordService.queryList(rechargeRecordBody);
        return getPageData(list);
    }

    @PostMapping("/export")
    public void export(HttpServletResponse response, TtRechargeRecordBody rechargeRecordBody) {
        List<TtRechargeRecord> list = rechargeRecordService.queryList(rechargeRecordBody);
        ExcelUtil<TtRechargeRecord> util = new ExcelUtil<>(TtRechargeRecord.class);
        util.exportExcel(response, list, "充值记录信息列表");
    }
}
