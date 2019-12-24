package io.coti.trustscore.services.calculationservices;


import io.coti.trustscore.data.tsbuckets.BucketData;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public abstract class BucketScoresCalculator<T extends BucketData> {

    protected T bucketData;

    public BucketScoresCalculator(T bucketData) {
        this.bucketData = bucketData;
    }

    public boolean decayScores() {
        if (!bucketData.lastUpdateBeforeToday()) {
            return false;
        }
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        int daysDiff = (int) ChronoUnit.DAYS.between(bucketData.getLastUpdate(), today);
        decayDailyScores(daysDiff);

        bucketData.setLastUpdate(today);
        return true;
    }

    protected void decayDailyScores(int daysDiff) {
    }
}
