package io.coti.trustscore.services.calculationServices;


import io.coti.trustscore.data.Buckets.BucketEventData;
import io.coti.trustscore.utils.DatesCalculation;

import java.util.Date;

public abstract class BucketCalculator {
    //abstract boolean decayScores();

    public abstract void setCurrentScores();

    public <T extends BucketEventData> boolean decayScores(T bucketEventData) {
        if (!bucketEventData.lastUpdateBeforeToday()) {
            return false;
        }
        int daysDiff = DatesCalculation.calculateDaysDiffBetweenDates(bucketEventData.getLastUpdate(), new Date());
        decayDailyEventScoresType(daysDiff);
        bucketEventData.setLastUpdate(DatesCalculation.setDateOnBeginningOfDay(new Date()));
        return true;
    }

    protected void decayDailyEventScoresType(int daysDiff) {

    }
}
