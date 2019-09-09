package io.coti.trustscore.services.calculationservices;

import io.coti.trustscore.data.scorebuckets.BucketDebtBalanceBasedScoreData;
import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.data.parameters.BalanceBasedUserParameters;
import io.coti.trustscore.data.parameters.CounteragentBalanceContributionData;
import io.coti.trustscore.services.BucketDebtBalanceBasedScoreService;
import io.coti.trustscore.utils.DatesCalculation;

public class BucketDebtBalanceBasedScoreCalculator extends BucketScoresCalculator<BucketDebtBalanceBasedScoreData> {
    private static final double ACCRUEMENT_FACTOR = 1.0/ 365.0;
    private BalanceBasedUserParameters debtUserParameters;

    public BucketDebtBalanceBasedScoreCalculator(BucketDebtBalanceBasedScoreData bucketData) {
        super(bucketData);
        debtUserParameters = (BalanceBasedUserParameters) BucketDebtBalanceBasedScoreService.userParameters(
                FinalScoreType.DEBT, bucketData.getUserType());
    }

    public void setCurrentScore(CounteragentBalanceContributionData counteragentData) {
        double gF =debtUserParameters.getWeight1() * (counteragentData.getCurrentBalance() > 0 ? 1 : 0) +
                debtUserParameters.getWeight2() * counteragentData.getCurrentBalance() / debtUserParameters.getLevel08();

        if (counteragentData.isRepayment()) {
            counteragentData.setTail(counteragentData.getTail() +
                    DatesCalculation.calculateDecay(debtUserParameters.getSemiDecay(), counteragentData.getOldFine(),1));
            counteragentData.setFine(gF * ACCRUEMENT_FACTOR);
        } else {
            counteragentData.setFine((gF + counteragentData.getFine()) * ACCRUEMENT_FACTOR + counteragentData.getFine());
        }
    }

    @Override
    protected void decayDailyScores(int daysDiff) {
        for (CounteragentBalanceContributionData counteragentBalanceContributionData : bucketData.getHashCounteragentBalanceContributionDataMap().values()) {

            counteragentBalanceContributionData.setTail(DatesCalculation.calculateDecay(debtUserParameters.getSemiDecay(),
                    counteragentBalanceContributionData.getTail(), daysDiff));
            decayFine(counteragentBalanceContributionData, daysDiff);
            counteragentBalanceContributionData.setRepayment(false);
        }
    }

    private void decayFine(CounteragentBalanceContributionData counteragentData, long daysDiff) {
        double f = counteragentData.getFine();
        double fOld = 0;
        double gF = debtUserParameters.getWeight1() * (counteragentData.getCurrentBalance() > 0 ? 1 : 0) +
                debtUserParameters.getWeight2() * counteragentData.getCurrentBalance() / debtUserParameters.getLevel08();

        if (gF != 0) {
            for (int i = 0; i < daysDiff; i++) {
                fOld = f;
                f = (gF + f) * ACCRUEMENT_FACTOR + f;
            }
        }
        counteragentData.setOldFine(fOld);
        counteragentData.setFine(f);
    }

    public double getBucketSumScore() {
        double sumScore = 0.0;

        for (CounteragentBalanceContributionData counteragentBalanceContributionData : bucketData.getHashCounteragentBalanceContributionDataMap().values()) {
            double gF = debtUserParameters.getWeight1() * (counteragentBalanceContributionData.getCurrentBalance() > 0 ? 1 : 0) +
                    debtUserParameters.getWeight2() * counteragentBalanceContributionData.getCurrentBalance() / debtUserParameters.getLevel08();
            sumScore += counteragentBalanceContributionData.getTail() + gF + counteragentBalanceContributionData.getFine();
        }
        return sumScore;
    }
}

