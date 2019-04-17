package io.coti.trustscore.services.calculationServices;


import io.coti.trustscore.data.Buckets.BucketEventData;
import io.coti.trustscore.utils.DatesCalculation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;

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

    }
}
