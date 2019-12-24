package io.coti.trustscore.services.calculationservices;

import io.coti.trustscore.data.parameters.BalanceBasedUserParameters;
import io.coti.trustscore.data.parameters.UserParameters;
import io.coti.trustscore.data.tsbuckets.BucketDepositBalanceBasedEventData;
import io.coti.trustscore.services.BucketDepositBalanceBasedScoreService;
import io.coti.trustscore.utils.DatesCalculation;

public class BucketDepositBalanceBasedScoreCalculator extends BucketScoresCalculator<BucketDepositBalanceBasedEventData> {
    private BalanceBasedUserParameters userParameters;

    public BucketDepositBalanceBasedScoreCalculator(BucketDepositBalanceBasedEventData bucketData) {
        super(bucketData);
        userParameters = (BalanceBasedUserParameters) BucketDepositBalanceBasedScoreService.userParameters(bucketData.getUserType());

    }

    @Override
    protected void decayDailyScores(int daysDiff) {
        bucketData.setTail(DatesCalculation.calculateDecay(userParameters.getSemiDecay(), bucketData.getTail(), daysDiff));
        bucketData.setCurrentDayClose(0);
    }

    public double getBucketSumScore() {
        return bucketData.getCurrentBalanceContribution() * userParameters.getWeight1() + bucketData.getTail() * userParameters.getWeight2();
    }

    public void setCurrentScores() {
        double currentDayCloseContribution = Math.tanh(bucketData.getCurrentDayClose() / userParameters.getLevel08() * UserParameters.ATANH08);
        bucketData.setCurrentBalanceContribution(Math.tanh(bucketData.getCurrentBalance() / userParameters.getLevel08() * UserParameters.ATANH08));
        bucketData.setTail(bucketData.getTail() + currentDayCloseContribution);
    }
}


