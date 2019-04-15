package io.coti.financialserver.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class DatesHelper {

    private DatesHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static Date getDateNumberOfDaysAfterToday(int numberOfDays) {

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        try {
            date = formatter.parse(formatter.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        DateUtils.addDays(date, numberOfDays);

        return date;
    }
}
