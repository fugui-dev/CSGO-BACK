package com.ruoyi.playingmethod.mapper;

import com.ruoyi.playingmethod.entity.TtAttendanceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface ApiAttendanceRecordMapper {
    TtAttendanceRecord selectByUid(@Param("uid") Integer uid);

    int insertAttendanceRecord(TtAttendanceRecord ttAttendanceRecord);


    TtAttendanceRecord selectSevenAttendance(@Param("uid") Integer uid,@Param("createTime") String createTime);

}
