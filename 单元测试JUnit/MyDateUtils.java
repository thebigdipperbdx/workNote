package com.sto.transport.event.infrastructure.util.common;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * LocalDateTime
 */
public class MyDateUtils {
    public static DateTimeFormatter formatterOne = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static DateTimeFormatter formatterTwo = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static DateTimeFormatter formatterThree = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static DateTimeFormatter formatterFour = DateTimeFormatter.ofPattern("HH");
    public static DateTimeFormatter formatterFive = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");


    public static String getNowDayTime() {
        return LocalDateTime.now().format(formatterOne);
    }

    public static String getNowTime() {
        return LocalDate.now().format(formatterTwo);
    }

    public static String getDayTimePlusByDay() {
        return LocalDateTime.now().plusDays(1).format(formatterOne);
    }

    public static String getDayPlusByDay(int day) {
        return LocalDate.now().plusDays(day).format(formatterTwo);
    }

    public static String getPersonDayTime(String actualDepartureTime) {
        LocalDateTime dayTime = LocalDateTime.parse(actualDepartureTime, formatterOne);
        int hour = Integer.parseInt(dayTime.format(formatterFour));
        if (hour < 12) {
            return dayTime.plusDays(-1).format(formatterTwo);
        }
        return dayTime.format(formatterTwo);
    }


    private static final LocalTime NOON_TIME = LocalTime.of(12, 00, 00);

    // 是否在中午12点之前
    public static boolean beforeNoontime() {
        return LocalTime.now().isBefore(NOON_TIME);
    }

    public static String yesterdayStart() {
        if (beforeNoontime()) {
            return getDayPlusByDay(-2) + " 12:00:00";
        }
        return getDayPlusByDay(-1) + " 12:00:00";
    }

    public static String yesterdayEnd() {
        if (beforeNoontime()) {
            return getDayPlusByDay(-1) + " 11:59:59";
        }
        return getDayPlusByDay(0) + " 11:59:59";
    }


    public static String getDateByDayStart(String startDay, int day) throws ParseException {
        LocalDateTime time = LocalDateTime.parse(startDay, formatterOne);
        time.plusDays(day);
        return time.format(formatterOne);
    }

    public static String getDateByDayEnd(String endDay, int day) throws ParseException {
        LocalDateTime time = LocalDateTime.parse(endDay, formatterOne);
        time.plusDays(day);
        return time.format(formatterOne);
    }

    public static String getSecondsDateTime(long millisecond) {
        LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(millisecond / 1000, 0, ZoneOffset.ofHours(8));
        return localDateTime.format(formatterOne);
    }

    public static long getModifiedOn(String modifiedOn) {
        return LocalDateTime.parse(modifiedOn, formatterFive).toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }


    public static void main(String[] args) {


    }
}
