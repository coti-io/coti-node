package io.coti.trustscore.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Slf4j
public class DatesCalculation {
    public static int calculateDaysDiffBetweenDates(Date firstDate, Date secondDate) {
        long difference = Math.abs(secondDate.getTime() - firstDate.getTime());
        return (int) (difference / (1000 * 60 * 60 * 24));
    }

    public static Date setDateOnBeginningOfDay(Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getYesterday() {
        return decreaseTodayDateByDays(1);
    }

    public static Date decreaseTodayDateByDays(int numberOfDays) {
        return new Date(setDateOnBeginningOfDay(new Date()).getTime() - 24 * (long) numberOfDays * 60 * 60 * 1000);
    }

    public static Date addToDateByDays(long milliSecondsDate, int numberOfDays) {
        return new Date(milliSecondsDate + 24 * (long) numberOfDays * 60 * 60 * 1000);
    }

}
