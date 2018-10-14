package io.coti.trustscore.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DatesCalculation {
    public static int calculateDaysdiffBetweenDates(Date dateBefore, Date dateAfter) {
        long difference = dateAfter.getTime() - dateBefore.getTime();
        float daysBetween = (difference / (1000 * 60 * 60 * 24));
        return (int) daysBetween;
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
}
