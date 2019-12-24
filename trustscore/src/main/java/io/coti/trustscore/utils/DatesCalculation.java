package io.coti.trustscore.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatesCalculation {

    private DatesCalculation() {}

    public static Double calculateDecay(long semiDecay, double value, int numberOfDecays) {
        return Math.exp(-Math.log(2) / semiDecay * numberOfDecays) * value;
    }
}


