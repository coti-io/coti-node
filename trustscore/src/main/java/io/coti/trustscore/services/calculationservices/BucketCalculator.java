package io.coti.trustscore.services.calculationservices;


import io.coti.trustscore.data.buckets.BucketEventData;
import io.coti.trustscore.utils.DatesCalculation;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
public abstract class BucketCalculator {

    public abstract void setCurrentScores();

    public <T extends BucketEventData> boolean decayScores(T bucketEventData) {
        if (!bucketEventData.lastUpdateBeforeToday()) {
            return false;
        }
        Date beginningOfDay = DatesCalculation.setDateOnBeginningOfDay(new Date());
        Date beginningOfLastUpdateDay = DatesCalculation.setDateOnBeginningOfDay(bucketEventData.getLastUpdate());
        int daysDiff = DatesCalculation.calculateDaysDiffBetweenDates(beginningOfLastUpdateDay, beginningOfDay);
        decayDailyEventScoresType(daysDiff);
        bucketEventData.setLastUpdate(beginningOfDay);
        return true;
    }

    protected void decayDailyEventScoresType(int daysDiff) {
        log.debug("Decay daily event scores type with {} day(s) difference", daysDiff);
    }
}
