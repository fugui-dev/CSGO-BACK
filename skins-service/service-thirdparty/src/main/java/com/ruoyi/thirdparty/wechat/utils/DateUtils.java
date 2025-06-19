package com.ruoyi.thirdparty.wechat.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
	
	/**
	 * 获取当前时间
	 * "yyyy-MM-dd"
	 * @return
	 */
	public static String queryCurrentDate(){
		Date d = new Date();  
        System.out.println(d);  
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
        return sdf.format(d);  
	}
	
	/**
	 * 获取当前时间
	 * "yyyy-MM-dd"
	 * @return
	 */
	public static String queryCurrentDay(){
		Date d = new Date();  
        System.out.println(d);  
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
        return sdf.format(d);  
	}
	
	public static String queryCurrentHour(){
		Date d = new Date();  
        System.out.println(d);  
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
        return sdf.format(d);  
	}
	
	public static String queryCurrentHourToEnd(){
		Date d = new Date();  
		Date dend = new Date(d.getTime()+300000);
        System.out.println(d);  
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String eString=sdf.format(dend);
        
        return eString;  
	}
	
	public static String longToString(long time){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String date = sdf.format(new Date(time*1000));
		return date; 
	}
	
	public static String longToStringMonth(long time){
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
		String date = sdf.format(new Date(time));
		return date; 
	}
	
	public static String longToStringQuan(long time){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sdf.format(new Date(time*1000));
		return date; 
	}
	
	public static String longToStringAll(long time){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sdf.format(new Date(time));
		return date; 
	}
	
	public static String dateStrclean(String dateStr){
		int index = dateStr.indexOf(".");
		dateStr = dateStr.substring(0, index);
		return dateStr;
	}
	
	public static String dateToStringShort(Date time){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String date = sdf.format(time);
		return date; 
	}
	
	public static String dateToStringLong(Date time){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sdf.format(time);
		return date; 
	}
	
	public static String dateToStringShortFormate(Date time){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String date = sdf.format(time);
		return date; 
	}
	
	public static String dateToDes(Date time){
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHH");
		String date = sdf.format(time);
		return date; 
	}
	
	public static int dateTimestamp(){
		return Integer.parseInt((new Date().getTime() + "").substring(
				0, 10));
	}
	
	public static Long stringToLong(String time){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;  
        try {  
            // Fri Feb 24 00:00:00 CST 2012  
            date = sdf.parse(time);   
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        // 2012-02-24  
        
		return date.getTime(); 
	}
	
	public static Date stringToDateLong(String time){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;  
        try {  
            // Fri Feb 24 00:00:00 CST 2012  
            date = sdf.parse(time);   
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        // 2012-02-24  
        
		return date; 
	}
	
	public static Timestamp getTimestamp(){
		
        try {  
            // Fri Feb 24 00:00:00 CST 2012  
        	Timestamp date = Timestamp.valueOf(dateToStringLong(new Date()));
        			return date; 
        } catch (Exception e) {  
            e.printStackTrace();  
            return null; 
        }  
        // 2012-02-24  
        
		
	}
	
	public static long geTimestamp(){
		
        try {  
            // Fri Feb 24 00:00:00 CST 2012  
        	long date = System.currentTimeMillis();
        			return date; 
        } catch (Exception e) {  
            e.printStackTrace();  
            return 0; 
        }  
        // 2012-02-24  
        
		
	}
	
	public static Date stringToDateShort(String time){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;  
        try {  
            // Fri Feb 24 00:00:00 CST 2012  
            date = sdf.parse(time);   
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        // 2012-02-24  
        
		return date; 
	}
	
	public static Date StringToDate(String time){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;  
        try {  
            // Fri Feb 24 00:00:00 CST 2012  
            date = sdf.parse(time);   
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        // 2012-02-24  
        
		return date;
	}
	
	public static String getWeekOfDate(Date dt) {
        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);

        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;

        return weekDays[w];
    }
	
	public static int getWeekIndexOfDate(Date dt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);

        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;

        return w;
    }
	
	public static int getMonthOfSystem(){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis( System.currentTimeMillis());
		int month = cal.get(Calendar.MONTH) + 1;
		return month;
	}
	
	public static int getYearOfSystem(){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis( System.currentTimeMillis());
		int year = cal.get(Calendar.YEAR);
		return year;
	}
	
	public static int getHourOfSystem(){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis( System.currentTimeMillis());
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		return hour;
	}
	
	public static int getDayOfDate(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date.getTime());
		int day = cal.get(Calendar.DAY_OF_MONTH);
		return day;
	}
	
	public static String getDayJiaByDay(int day){
		SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date beginDate = new Date();
		Calendar date = Calendar.getInstance();
		date.setTime(beginDate);
		date.set(Calendar.DATE, date.get(Calendar.DATE) + day);
		String endDate = dft.format(date.getTime());
		return endDate;
	}
	
	public static String getDayJiaByDay(Date date1 ,int day){
		SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date beginDate = date1;
		Calendar date = Calendar.getInstance();
		date.setTime(beginDate);
		date.set(Calendar.DATE, date.get(Calendar.DATE) + day);
		String endDate = dft.format(date.getTime());
		return endDate;
	}
	

	public static String getDayShortJiaByDay(Date date1 ,int day){
		SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd");
		Date beginDate = date1;
		Calendar date = Calendar.getInstance();
		date.setTime(beginDate);
		date.set(Calendar.DATE, date.get(Calendar.DATE) + day);
		String endDate = dft.format(date.getTime());
		return endDate;
	}
	
	public static boolean belongCalendar(Date time, Date from, Date to) {
        Calendar date = Calendar.getInstance();
        date.setTime(time);

        Calendar after = Calendar.getInstance();
        after.setTime(from);

        Calendar before = Calendar.getInstance();
        before.setTime(to);

        if (date.after(after) && date.before(before)) {
            return true;
        } else {
            return false;
        }
    }

//	public static void main(String[] args) throws Exception {
//		
//		Date now = DateUtils.stringToDateShort("2017-10-18");
//		Date from = DateUtils.stringToDateShort("2017-10-18");
//		Date to = DateUtils.stringToDateShort("2017-10-18");
//		
//		System.out.println(DateUtils.belongCalendar(now, from, to));
//		
//	}

}
