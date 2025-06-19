package com.ruoyi.playingmethod.service;

import com.ruoyi.playingmethod.entity.TtAttendanceRecord;

import java.util.List;

public interface IApiAttendanceRecordService {

    TtAttendanceRecord selectByUid(int uid);

    int insert(int uid);

    List<TtAttendanceRecord> selectSevenAttendance(int uid);
}
