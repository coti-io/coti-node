package io.coti.trustscore.services.interfaces;

import io.coti.trustscore.data.scorebuckets.BucketData;
import io.coti.trustscore.data.scoreenums.ScoreType;
import io.coti.trustscore.data.scoreevents.ScoreData;

public interface IBucketService<T extends ScoreData, S extends BucketData> {
    S addScoreToCalculations(T scoreData, S bucketData);

    double getBucketSumScore(S bucketData);

    ScoreType getScoreType();
}
