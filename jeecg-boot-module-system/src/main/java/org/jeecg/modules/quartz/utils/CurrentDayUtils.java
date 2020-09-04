package org.jeecg.modules.quartz.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CurrentDayUtils {
    public static final String DATE_FORMAT_FULL = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT_YMD = "yyyy-MM-dd";
    public static final String DATE_FORMAT_HMS = "HH:mm:ss";
    public static final String DATE_FORMAT_HM = "HH:mm";
    public static final String DATE_FORMAT_YMDHM = "yyyy-MM-dd HH:mm";
    public static final String DATE_FORMAT_YMDHMS = "yyyyMMddHHmmss";
    public static final long ONE_DAY_MILLS = 3600000 * 24;
    public static final int WEEK_DAYS = 7;
    private static final int dateLength = DATE_FORMAT_YMDHM.length();

    /**
     * 日期转换为制定格式字符串
     *
     * @param time
     * @param format
     * @return String
     */
    public static String formatDateToString(Date time, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(time);
    }
    /**
     * 字符串转换为制定格式日期
     * (注意：当你输入的日期是2014-12-21 12:12，format对应的应为yyyy-MM-dd HH:mm
     * 否则异常抛出)
     * @param date
     * @param format
     * @return Date
     * @throws ParseException
     *       @
     */
    private static Date formatStringToDate(String date, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(date);
        }

    /**
     * 获取指定日期星期几
     *
     * @param datetime
     * @throws ParseException
     * @return 返回的是以周日为第一天的一周，1对应周日，7对应周六
     *       @
     */
    public static int getWeekOfDate(String datetime) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setMinimalDaysInFirstWeek(WEEK_DAYS);
        Date date = formatStringToDate(datetime, DATE_FORMAT_YMD);
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * 以yyyy-MM-DD的格式返回今天
     * @return yyyy-MM-DD
     */
    public static String getToday() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    /**
     * 以yyyyMMDD的格式返回今天
     * @return yyyyMMDD
     */
    public static String getTodaytoYYYYMMDD() {
        return new SimpleDateFormat("yyyyMMdd").format(new Date());
    }

    /**
     * 得到所属的星期数
     * @param date
     * @return int,将对应的星期数返回，1代表周一，7代表周日，0为异常数据
     */
    public static int getWeekStr(Date date){
        Calendar cal = Calendar.getInstance();
        if(date!=null) {
            cal.setTime(date);
        }
        int week  = cal.get(Calendar.DAY_OF_WEEK);
        switch (week) {
            case 1:
                return 7;
            case 2:
                return 1;
            case 3:
                return 2;
            case 4:
                return 3;
            case 5:
                return 4;
            case 6:
                return 5;
            case 7:
                return 6;
        }
        return 0;
    }

    public static String getYesterday(){
        DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar=Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,-24);
        return dateFormat.format(calendar.getTime());
    }
//查询工时的时候可能会用到
    public static String getBeforeDay(){
        if(getWeekStr(new Date()) == 1){
            return getFourDaysAgo();
        }
        else{
            return getYesterday();
        }
    }

    public static String getAfterDay(){
        if(getWeekStr(new Date()) == 1){
            return getThreeDaysAgo();
        }
        else{
            return getToday();
        }
    }

    public static String getThreeDaysAgo (){
        DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar=Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,-48);
        return dateFormat.format(calendar.getTime());
    }

    public static String getFourDaysAgo (){
        DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar=Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,-72);
        return dateFormat.format(calendar.getTime());
    }

    public static void main(String[] args) throws ParseException {
        System.out.println(getYesterday());
        System.out.println(getToday());
    }
}
