package io.coti.trustscore.services.calculationservices;

import io.coti.trustscore.data.scorebuckets.BucketChargeBackFrequencyBasedScoreData;
import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.data.parameters.ChargeBackUserParameters;
import io.coti.trustscore.services.BucketChargeBackFrequencyBasedScoreService;
import io.coti.trustscore.utils.DatesCalculation;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;

@Slf4j
public class BucketChargeBackFrequencyBasedScoreCalculator extends BucketScoresCalculator<BucketChargeBackFrequencyBasedScoreData> {
    private static final int MONTH_LENGTH = 30;
    private ChargeBackUserParameters userParameters;

    public BucketChargeBackFrequencyBasedScoreCalculator(BucketChargeBackFrequencyBasedScoreData bucketChargeBackFrequencyBasedScoreData) {
        super(bucketChargeBackFrequencyBasedScoreData);
        userParameters = (ChargeBackUserParameters) BucketChargeBackFrequencyBasedScoreService.userParameters(
                FinalScoreType.CHARGEBACK, bucketChargeBackFrequencyBasedScoreData.getUserType());
    }

    public void setCurrentScores() {
        if (userParameters == null) return;

        double amountContribution = Math.max(bucketData.getCurrentMonthAmountOfChargeBacks().values().stream().mapToDouble(Number::doubleValue).sum() /
                        bucketData.getCurrentMonthAmountOfPaymentTransactions().values().stream().mapToDouble(Number::doubleValue).sum() -
                        userParameters.getStandardChargeBackRate(),
                0);
        if (Double.isNaN(amountContribution)) {
            amountContribution = 0.0;
        }
        bucketData.setCurrentMonthContributionOfChargeBacksAmount(amountContribution);

        double numberContribution = Math.max(bucketData.getCurrentMonthNumberOfChargeBacks().values().stream().mapToDouble(Number::doubleValue).sum() /
                        bucketData.getCurrentMonthNumberOfPaymentTransactions().values().stream().mapToDouble(Number::doubleValue).sum() -
                        userParameters.getStandardChargeBackRate(),
                0);
        if (Double.isNaN(numberContribution)) {
            numberContribution = 0.0;
        }
        bucketData.setCurrentMonthContributionOfChargeBacksNumber(numberContribution);
    }

    @Override
    protected void decayDailyScores(int daysDiff) {
        LocalDate startDate = LocalDate.now(ZoneOffset.UTC).minusDays(MONTH_LENGTH);

        bucketData.setOldDatesContributionOfChargeBacksAmount(DatesCalculation.calculateDecay(userParameters.getAmountSemiDecay(),
                bucketData.getOldDatesContributionOfChargeBacksAmount() + bucketData.getCurrentMonthContributionOfChargeBacksAmount(),
                daysDiff));

        bucketData.setOldDatesContributionOfChargeBacksNumber(DatesCalculation.calculateDecay(userParameters.getNumberSemiDecay(),
                bucketData.getOldDatesContributionOfChargeBacksNumber() + bucketData.getCurrentMonthContributionOfChargeBacksNumber(),
                daysDiff));

        synchronized (this) {
            for (Map.Entry<LocalDate, Double> amountOfChargeBacks : bucketData.getCurrentMonthAmountOfChargeBacks().entrySet()) {
                if (amountOfChargeBacks.getKey().isBefore(startDate)) {
                    bucketData.getCurrentMonthNumberOfChargeBacks().remove(amountOfChargeBacks.getKey());
                    bucketData.getCurrentMonthAmountOfChargeBacks().remove(amountOfChargeBacks.getKey());
                }
            }
        }

        synchronized (this) {
            for (Map.Entry<LocalDate, Double> amountOfTransactions : bucketData.getCurrentMonthAmountOfPaymentTransactions().entrySet()) {
                if (amountOfTransactions.getKey().isBefore(startDate)) {
                    bucketData.getCurrentMonthNumberOfPaymentTransactions().remove(amountOfTransactions.getKey());
                    bucketData.getCurrentMonthAmountOfPaymentTransactions().remove(amountOfTransactions.getKey());
                }
            }
        }
    }

    public double getBucketSumScore(BucketChargeBackFrequencyBasedScoreData bucketData) {
        double amountCalculationScore = bucketData.getCurrentMonthContributionOfChargeBacksAmount() * userParameters.getAmountWeight1() +
                bucketData.getOldDatesContributionOfChargeBacksAmount() * userParameters.getAmountWeight2();
        double numberCalculationScore = bucketData.getCurrentMonthContributionOfChargeBacksNumber() * userParameters.getNumberWeight1() +
                bucketData.getOldDatesContributionOfChargeBacksNumber() * userParameters.getNumberWeight2();

//        return amountCalculationScore + numberCalculationScore;
        return 0.0;
    }
}
