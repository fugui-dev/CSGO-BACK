package com.ruoyi.admin.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.domain.entity.TtPromotionRecord;
import com.ruoyi.admin.service.TtPromotionRecordService;
import com.ruoyi.domain.vo.PromotionDataVO;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.PageDataInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/promotionRecord")
public class TtPromotionRecordController extends BaseController {

    private final TtPromotionRecordService promotionRecordService;

    public TtPromotionRecordController(TtPromotionRecordService promotionRecordService) {
        this.promotionRecordService = promotionRecordService;
    }

    @GetMapping("/getPromotionRecord")
    public PageDataInfo<TtPromotionRecord> getPromotionRecord(TtPromotionRecord ttPromotionRecord){
        startPage();
        List<TtPromotionRecord> list = promotionRecordService.getPromotionRecord(ttPromotionRecord);
        return getPageData(list);
    }


    /**
     * 统计用户充值网推广数据
     * @param userId
     * @return
     */
    @GetMapping("/statisticsPromotionData/{userId}")
    public R<PromotionDataVO> statisticsPromotionData(@PathVariable("userId") Integer userId){
        PromotionDataVO data = promotionRecordService.statisticsPromotionData(userId);
        return R.ok(data);
    }
}
