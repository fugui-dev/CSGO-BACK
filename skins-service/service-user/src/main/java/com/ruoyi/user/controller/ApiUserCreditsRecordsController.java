package com.ruoyi.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.admin.service.TtUserCreditsRecordsService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.domain.entity.recorde.TtUserCreditsRecords;
import com.ruoyi.domain.vo.TtUserCreditsRecordsRankVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.Calendar;

@Api(tags = "用户信用记录")
@RestController
@RequestMapping("/api/userCreditsRecords")
@Slf4j
public class ApiUserCreditsRecordsController extends BaseController {

    private final TtUserCreditsRecordsService ttUserCreditsRecordsService;

    public ApiUserCreditsRecordsController(TtUserCreditsRecordsService ttUserCreditsRecordsService) {
        this.ttUserCreditsRecordsService = ttUserCreditsRecordsService;
    }

    /**
     * 弹药排行榜
     * @param type 1今天 2昨天 3近一周
     */
    @ApiOperation("流水网流水排行榜")
    @GetMapping("/creditsRank/{type}/{page}/{size}")
    @Anonymous
    public Page<TtUserCreditsRecordsRankVO> creditsRank(@PathVariable("type") Integer type,
                                                        @PathVariable("page") Integer page,
                                                        @PathVariable("size") Integer size) {

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,0);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);
        Timestamp end = null;
        Timestamp begin = null;

        if (type.equals(1)) {
            begin = new Timestamp(c.getTimeInMillis());
            end = new Timestamp(System.currentTimeMillis());
        } else if (type.equals(2)) {
            end = new Timestamp(c.getTimeInMillis());
            c.add(Calendar.DAY_OF_MONTH,-1);
            begin = new Timestamp(c.getTimeInMillis());
        } else if (type.equals(3)) {
            end = new Timestamp(c.getTimeInMillis());
            c.add(Calendar.WEEK_OF_MONTH,-1);
            begin = new Timestamp(c.getTimeInMillis());
        }

        return ttUserCreditsRecordsService.rank(begin, end, page, size);
    }

    /**
     * 流水网推广奖励明细
     * @param type 1、今天；2、昨天；3、近七天
     */
    @ApiOperation("流水网推广奖励明细")
    @GetMapping("/pWelfareRecords/{type}/{page}/{size}")
    public Page<TtUserCreditsRecords> pWelfareRecords(@PathVariable("type") Integer type,
                                                      @PathVariable("page") Integer page,
                                                      @PathVariable("size") Integer size) {
        Long userId = getUserId();
        return ttUserCreditsRecordsService.pWelfareRecords(Integer.valueOf(String.valueOf(userId)),type,page,size);
    }
}
