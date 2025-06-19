package com.ruoyi.admin.controller;

import com.ruoyi.admin.service.TtRedPacketRecordService;
import com.ruoyi.domain.other.TtRedPacketRecordBody;
import com.ruoyi.domain.vo.TtRedPacketRecordDataVO;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.PageDataInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/redPacketRecord")
public class TtRedPacketRecordController extends BaseController {

    private final TtRedPacketRecordService redPacketRecordService;

    public TtRedPacketRecordController(TtRedPacketRecordService redPacketRecordService) {
        this.redPacketRecordService = redPacketRecordService;
    }

    @GetMapping("/getRedPacketRecordList")
    public PageDataInfo<TtRedPacketRecordDataVO> getRedPacketRecordList(TtRedPacketRecordBody ttRedPacketRecordBody) {
        startPage();
        List<TtRedPacketRecordDataVO> list = redPacketRecordService.queryList(ttRedPacketRecordBody);
        return getPageData(list);
    }
}
