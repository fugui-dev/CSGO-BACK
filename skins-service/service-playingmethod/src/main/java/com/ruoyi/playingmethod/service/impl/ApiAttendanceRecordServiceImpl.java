package com.ruoyi.playingmethod.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.ruoyi.playingmethod.entity.TtAttendanceRecord;
import com.ruoyi.playingmethod.mapper.ApiAttendanceRecordMapper;
import com.ruoyi.playingmethod.service.IApiAttendanceRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class ApiAttendanceRecordServiceImpl implements IApiAttendanceRecordService{
    private final ApiAttendanceRecordMapper apiTtAttendanceRecordMapper;

    public ApiAttendanceRecordServiceImpl(ApiAttendanceRecordMapper apiTtAttendanceRecordMapper) {
        this.apiTtAttendanceRecordMapper = apiTtAttendanceRecordMapper;
    }


    @Override
    public TtAttendanceRecord selectByUid(int uid) {
        return apiTtAttendanceRecordMapper.selectByUid(uid);
    }

    @Override
    public int insert(int uid) {
        TtAttendanceRecord ttAttendanceRecord = new TtAttendanceRecord();
        ttAttendanceRecord.setUserId(uid);
        ttAttendanceRecord.setCreateTime(new Date());
        ttAttendanceRecord.setCoin(1);
        return apiTtAttendanceRecordMapper.insertAttendanceRecord(ttAttendanceRecord);

    }

    @Override
    public List<TtAttendanceRecord> selectSevenAttendance(int uid) {
        List<TtAttendanceRecord> ttAttendanceRecords = new ArrayList<>();
        //周一
        DateTime dateTime = DateUtil.beginOfWeek(new Date());
        System.out.println(new SimpleDateFormat("yyyy-MM-dd").format(dateTime));
        TtAttendanceRecord zhouYittAttendanceRecor = new TtAttendanceRecord();

        if (apiTtAttendanceRecordMapper.selectSevenAttendance(uid,new SimpleDateFormat("yyyy-MM-dd").format(dateTime)) != null )zhouYittAttendanceRecor.setIsStatus("1");
        zhouYittAttendanceRecor.setCreateTime(dateTime);
        ttAttendanceRecords.add(zhouYittAttendanceRecor);
        //周二
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(dateTime);
        calendar.add(Calendar.DATE, 1);
        System.out.println(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));

        TtAttendanceRecord zhouErttAttendanceRecord = new TtAttendanceRecord();
        if (apiTtAttendanceRecordMapper.selectSevenAttendance(uid,new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime())) != null)zhouErttAttendanceRecord.setIsStatus("1");
        zhouErttAttendanceRecord.setCreateTime(calendar.getTime());
        ttAttendanceRecords.add(zhouErttAttendanceRecord);

        //周三
        calendar.add(Calendar.DATE, 1);
        System.out.println(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));

        TtAttendanceRecord zhouSanttAttendanceRecord = new TtAttendanceRecord();
        if (apiTtAttendanceRecordMapper.selectSevenAttendance(uid,new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()))  != null )zhouSanttAttendanceRecord.setIsStatus("1");
        zhouSanttAttendanceRecord.setCreateTime(calendar.getTime());
        ttAttendanceRecords.add(zhouSanttAttendanceRecord);

        //周四
        calendar.add(Calendar.DATE, 1);
        System.out.println(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));

        TtAttendanceRecord zhouSittAttendanceRecord = new TtAttendanceRecord();
        if (apiTtAttendanceRecordMapper.selectSevenAttendance(uid,new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime())) != null )zhouSittAttendanceRecord.setIsStatus("1");
        zhouSittAttendanceRecord.setCreateTime(calendar.getTime());
        ttAttendanceRecords.add(zhouSittAttendanceRecord);

        //周五
        calendar.add(Calendar.DATE, 1);
        System.out.println(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));

        TtAttendanceRecord zhouWuttAttendanceRecord = new TtAttendanceRecord();
        if (apiTtAttendanceRecordMapper.selectSevenAttendance(uid,new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime())) != null )zhouWuttAttendanceRecord.setIsStatus("1");
        zhouWuttAttendanceRecord.setCreateTime(calendar.getTime());
        ttAttendanceRecords.add(zhouWuttAttendanceRecord);

        //周六
        calendar.add(Calendar.DATE, 1);
        System.out.println(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));

        TtAttendanceRecord zhouLiuttAttendanceRecord = new TtAttendanceRecord();
        if (apiTtAttendanceRecordMapper.selectSevenAttendance(uid,new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime())) != null )zhouLiuttAttendanceRecord.setIsStatus("1");
        zhouLiuttAttendanceRecord.setCreateTime(calendar.getTime());
        ttAttendanceRecords.add(zhouLiuttAttendanceRecord);

        //周天
        calendar.add(Calendar.DATE, 1);
        System.out.println(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));

        TtAttendanceRecord zhouRittAttendanceRecord = new TtAttendanceRecord();
        if (apiTtAttendanceRecordMapper.selectSevenAttendance(uid,new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime())) != null )zhouRittAttendanceRecord.setIsStatus("1");
        zhouRittAttendanceRecord.setCreateTime(calendar.getTime());
        ttAttendanceRecords.add(zhouRittAttendanceRecord);

        return ttAttendanceRecords;
    }

}
