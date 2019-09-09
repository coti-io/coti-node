package io.coti.trustscore.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Slf4j
public class DatesCalculation {
    public static final int MILLI_SECONDS_PER_SECOND = 1000;

    public static Double calculateDecay(long semiDecay, double value, int numberOfDecays) {
        return Math.exp(-Math.log(2) / semiDecay * numberOfDecays) * value;
    }
}


