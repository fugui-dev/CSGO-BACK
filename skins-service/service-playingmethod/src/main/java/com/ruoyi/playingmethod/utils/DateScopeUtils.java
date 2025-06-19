package com.ruoyi.playingmethod.utils;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

public class DateScopeUtils {

    static String dateFormat = "yyyy-MM-dd HH:mm:ss";

    public static Date getTodayBegin(){
        // 获取当天的开始时间
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendar.getTime();
        return startOfDay;
    }

    public static Date getTodayEnd(){
        // 获取当天的结束时间
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date endOfDay = calendar.getTime();
        return endOfDay;
    }

    public static LocalDateTime getWeekTimeStart (){
        LocalDate currentDate = LocalDate.now();

        // 获取本周周一的日期
        LocalDate mondayOfThisWeek = currentDate.with(DayOfWeek.MONDAY);
        // 设置时间为 0 点 0 分 0 秒

        return LocalDateTime.of(mondayOfThisWeek, LocalTime.MIN);

    }

    public static LocalDateTime getMonthTimeStart (){
        // 获取当前日期
        LocalDate currentDate1 = LocalDate.now();
        // 获取本月一号
        LocalDate firstDay = currentDate1.withDayOfMonth(1);

        return LocalDateTime.of(firstDay, LocalTime.MIN);

    }

    public static LocalDateTime getNow (){
        return LocalDateTime.now();

    }

}
