package com.artemisia_corp.artemisia.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    public static final String FORMAT_ISO_8601_FULL = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_ISO_8601_SHORT = "yyyy-MM-dd";

    public static Date formatToStart(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date formatToEnd(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }
}