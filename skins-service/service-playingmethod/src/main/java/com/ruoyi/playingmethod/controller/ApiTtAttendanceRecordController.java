package com.ruoyi.playingmethod.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.playingmethod.entity.TtAttendanceRecord;
import com.ruoyi.playingmethod.service.IApiAttendanceRecordService;
import io.swagger.annotations.Api;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "签到记录")
@RestController
@RequestMapping("/api/attendanceRecord")
public class ApiTtAttendanceRecordController extends BaseController {
    private final IApiAttendanceRecordService iApiAttendanceRecordService;

    public ApiTtAttendanceRecordController(IApiAttendanceRecordService iApiAttendanceRecordService) {
        this.iApiAttendanceRecordService = iApiAttendanceRecordService;
    }

    /**
     * 签到
     */
    // @GetMapping("/attendance")
    public R<String> attendance() {
        System.out.println("==================签到=================");
        TtAttendanceRecord ttAttendanceRecord = iApiAttendanceRecordService.selectByUid(getUserId().intValue());
    if (ttAttendanceRecord == null){
        // 增加签到记录
        iApiAttendanceRecordService.insert(getUserId().intValue());
        return R.ok("签到成功");
    }
    // List<UserPackSackDataVO> userCompoundRecord = iApiAttendanceRecordService.getUserCompoundRecord(getUserId().intValue());
        return R.fail("签到失败,一天只能签到一次");
    }

    /**
     * 七天签到记录
     */
    // @GetMapping("/sevenAttendance")
    public R<List<TtAttendanceRecord>> sevenAttendance() {
        System.out.println("==================七天签到=================");
        // TtAttendanceRecord ttAttendanceRecord = iApiAttendanceRecordService.selectByUid(getUserId().intValue());
        List<TtAttendanceRecord> ttAttendanceRecordList = iApiAttendanceRecordService.selectSevenAttendance(getUserId().intValue());
        return R.ok(ttAttendanceRecordList);
    }

}
