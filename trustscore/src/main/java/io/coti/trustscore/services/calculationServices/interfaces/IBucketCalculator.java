package io.coti.trustscore.services.calculationServices.interfaces;


public interface IBucketCalculator {
    void decayScores();

    void setCurrentScores();
}
