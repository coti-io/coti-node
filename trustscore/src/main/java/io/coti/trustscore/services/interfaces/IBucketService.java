package io.coti.trustscore.services.interfaces;

import io.coti.trustscore.data.tsbuckets.BucketData;
import io.coti.trustscore.data.tsenums.EventType;
import io.coti.trustscore.data.tsevents.EventData;

public interface IBucketService<T extends EventData, S extends BucketData> {
    S addScoreToCalculations(T scoreData, S bucketData);

    double getBucketSumScore(S bucketData);

    EventType getScoreType();
}
