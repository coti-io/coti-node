package io.coti.trustscore.bl.BucketCalculator;



public interface IBucketCalculator {
    void decayScores();
    void setCurrentScores();
}
