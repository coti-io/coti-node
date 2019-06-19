package io.coti.trustscore.services.calculationservices;

import io.coti.trustscore.config.rules.BehaviorHighFrequencyEventsScore;
import io.coti.trustscore.config.rules.HighFrequencyEventScore;
import io.coti.trustscore.config.rules.RulesData;
import io.coti.trustscore.data.Buckets.BucketChargeBackEventsData;
import io.coti.trustscore.data.Enums.HighFrequencyEventScoreType;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.utils.DatesCalculation;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class BucketChargeBackEventsCalculator extends BucketCalculator {
    private static Map<UserType, BehaviorHighFrequencyEventsScore> userTypeToBehaviorHighFrequencyEventsScoreMapping;
    private BucketChargeBackEventsData bucketChargeBackEventsData;
    private BehaviorHighFrequencyEventsScore behaviorHighFrequencyEventsScore;

    public BucketChargeBackEventsCalculator(BucketChargeBackEventsData bucketChargeBackEventsData) {
        this.bucketChargeBackEventsData = bucketChargeBackEventsData;
        behaviorHighFrequencyEventsScore = userTypeToBehaviorHighFrequencyEventsScoreMapping.get(bucketChargeBackEventsData.getUserType());

    }

    public static void init(RulesData rulesData) {
        userTypeToBehaviorHighFrequencyEventsScoreMapping = rulesData.getUserTypeToUserScoreMap().entrySet().stream().
                collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getBehaviorHighFrequencyEventsScore()));
    }

    @Override
    public void setCurrentScores() {
        Map<HighFrequencyEventScore, Double> eventScoresToCalculatedScoreMap = new ConcurrentHashMap<>();
        HighFrequencyEventScore chargeBackEventScoreByEventScoreType = behaviorHighFrequencyEventsScore.getHighFrequencyEventScoreMap().get(HighFrequencyEventScoreType.CHARGE_BACK_AMOUNT);
        if (chargeBackEventScoreByEventScoreType == null) return;

        eventScoresToCalculatedScoreMap.put(chargeBackEventScoreByEventScoreType,
                    Math.max((bucketChargeBackEventsData.getOldDateAmountOfChargeBacksContribution()
                            + bucketChargeBackEventsData.getCurrentDateChargeBacks().values().stream().mapToDouble(Number::doubleValue).sum())
                            / (bucketChargeBackEventsData.getOldDateAmountOfCreditTransactionsContribution()
                            + bucketChargeBackEventsData.getCurrentDatePaymentTransactions().values().stream().mapToDouble(Number::doubleValue).sum())
                            - chargeBackEventScoreByEventScoreType.getStandardChargeBackRate(), 0));

        eventScoresToCalculatedScoreMap.put(behaviorHighFrequencyEventsScore.getHighFrequencyEventScoreMap()
                        .get(HighFrequencyEventScoreType.CHARGE_BACK_NUMBER),
                    Math.max((bucketChargeBackEventsData.getOldDateNumberOfChargeBacksContribution()
                            + bucketChargeBackEventsData.getCurrentDateChargeBacks().size())
                            / (bucketChargeBackEventsData.getOldDateNumberOfCreditTransactionsContribution()
                            + bucketChargeBackEventsData.getCurrentDatePaymentTransactions().size())
                            - chargeBackEventScoreByEventScoreType.getStandardChargeBackRate(), 0));

        for (HighFrequencyEventScore highFrequencyEventScore : eventScoresToCalculatedScoreMap.keySet())
            if (eventScoresToCalculatedScoreMap.get(highFrequencyEventScore).isNaN()) {
                eventScoresToCalculatedScoreMap.put(highFrequencyEventScore, 0.0);
            }
        updateBucketScoresByFunction(eventScoresToCalculatedScoreMap);
    }

    private HighFrequencyEventScore getChargeBackEventScoreByEventScoreType(HighFrequencyEventScoreType highFrequencyEventScore) {
        return behaviorHighFrequencyEventsScore.getHighFrequencyEventScoreMap().get(highFrequencyEventScore);
    }

    private void decayChargeBackAmount(int daysDiff) {
        Map<HighFrequencyEventScore, Double> highFrequencyEventScoreToCalculationFormulaMap = new ConcurrentHashMap<>();

        double currentDateCountOfChargeBacks = bucketChargeBackEventsData.getCurrentDateChargeBacks().values().stream().mapToDouble(Number::doubleValue).sum();
        HighFrequencyEventScore chargeBackEventScoreByEventScoreType = getChargeBackEventScoreByEventScoreType(HighFrequencyEventScoreType.CHARGE_BACK_AMOUNT);
        if (chargeBackEventScoreByEventScoreType != null) {
            highFrequencyEventScoreToCalculationFormulaMap.put(chargeBackEventScoreByEventScoreType,
                    currentDateCountOfChargeBacks
                            + bucketChargeBackEventsData.getOldDateAmountOfChargeBacksContribution());

            Map<HighFrequencyEventScore, Double> highFrequencyEventScoreDecayedScores = new DecayCalculator(highFrequencyEventScoreToCalculationFormulaMap).calculate(daysDiff);
            bucketChargeBackEventsData.setOldDateAmountOfChargeBacksContribution(highFrequencyEventScoreDecayedScores.get(behaviorHighFrequencyEventsScore.getHighFrequencyEventScoreMap()
                    .get(HighFrequencyEventScoreType.CHARGE_BACK_AMOUNT)));
        }
    }

    private void decayPaymentTransactionAmount(int daysDiff) {

        double currentDateCountOfCreditTransactions = bucketChargeBackEventsData.getCurrentDatePaymentTransactions().values().stream().mapToDouble(Number::doubleValue).sum();
        Map<HighFrequencyEventScore, Double> highFrequencyEventScoreToCalculationFormulaMap = new ConcurrentHashMap<>();

        HighFrequencyEventScore chargeBackEventScoreByEventScoreType = getChargeBackEventScoreByEventScoreType(HighFrequencyEventScoreType.CHARGE_BACK_AMOUNT);
        if (chargeBackEventScoreByEventScoreType != null) {
            highFrequencyEventScoreToCalculationFormulaMap.put(chargeBackEventScoreByEventScoreType,
                    currentDateCountOfCreditTransactions
                            + bucketChargeBackEventsData.getOldDateAmountOfCreditTransactionsContribution());

            Map<HighFrequencyEventScore, Double> highFrequencyEventScoreDecayedScores = new DecayCalculator(highFrequencyEventScoreToCalculationFormulaMap).calculate(daysDiff);
            bucketChargeBackEventsData.setOldDateAmountOfCreditTransactionsContribution(highFrequencyEventScoreDecayedScores.get(behaviorHighFrequencyEventsScore.getHighFrequencyEventScoreMap()
                    .get(HighFrequencyEventScoreType.CHARGE_BACK_AMOUNT)));
        }

    }

    private void decayChargeBackNumber(int daysDiff) {
        Map<HighFrequencyEventScore, Double> highFrequencyEventScoreToCalculationFormulaMap = new ConcurrentHashMap<>();

        double currentDateNumberOfChargeBacks = bucketChargeBackEventsData.getCurrentDateChargeBacks().size();
        HighFrequencyEventScore chargeBackEventScoreByEventScoreType = getChargeBackEventScoreByEventScoreType(HighFrequencyEventScoreType.CHARGE_BACK_NUMBER);
        if (chargeBackEventScoreByEventScoreType != null) {
            highFrequencyEventScoreToCalculationFormulaMap.put(chargeBackEventScoreByEventScoreType,
                    currentDateNumberOfChargeBacks
                            + bucketChargeBackEventsData.getOldDateNumberOfChargeBacksContribution());

            Map<HighFrequencyEventScore, Double> highFrequencyEventScoreDecayedScores = new DecayCalculator(highFrequencyEventScoreToCalculationFormulaMap).calculate(daysDiff);
            bucketChargeBackEventsData.setOldDateNumberOfChargeBacksContribution(highFrequencyEventScoreDecayedScores.get(behaviorHighFrequencyEventsScore.getHighFrequencyEventScoreMap()
                    .get(HighFrequencyEventScoreType.CHARGE_BACK_NUMBER)));
        }
    }

    private void decayPaymentTransactionNumber(int daysDiff) {

        double currentDateNumberOfCreditTransactions = bucketChargeBackEventsData.getCurrentDatePaymentTransactions().size();
        Map<HighFrequencyEventScore, Double> highFrequencyEventScoreToCalculationFormulaMap = new ConcurrentHashMap<>();

        HighFrequencyEventScore chargeBackEventScoreByEventScoreType = getChargeBackEventScoreByEventScoreType(HighFrequencyEventScoreType.CHARGE_BACK_NUMBER);
        if (chargeBackEventScoreByEventScoreType != null) {
            highFrequencyEventScoreToCalculationFormulaMap.put(chargeBackEventScoreByEventScoreType,
                    currentDateNumberOfCreditTransactions
                            + bucketChargeBackEventsData.getOldDateNumberOfCreditTransactionsContribution());

            Map<HighFrequencyEventScore, Double> highFrequencyEventScoreDecayedScores = new DecayCalculator(highFrequencyEventScoreToCalculationFormulaMap).calculate(daysDiff);
            bucketChargeBackEventsData.setOldDateNumberOfCreditTransactionsContribution(highFrequencyEventScoreDecayedScores.get(behaviorHighFrequencyEventsScore.getHighFrequencyEventScoreMap()
                    .get(HighFrequencyEventScoreType.CHARGE_BACK_NUMBER)));
        }
    }

    @Override
    protected void decayDailyEventScoresType(int daysDiff) {
        copyDailyEventsToOldDateChargeBacksMap(bucketChargeBackEventsData);
        decayChargeBackAmount(daysDiff);
        decayPaymentTransactionAmount(daysDiff);
        decayChargeBackNumber(daysDiff);
        decayPaymentTransactionNumber(daysDiff);
        bucketChargeBackEventsData.getCurrentDateChargeBacks().clear();
        bucketChargeBackEventsData.getCurrentDatePaymentTransactions().clear();
    }

    private void copyDailyEventsToOldDateChargeBacksMap(BucketChargeBackEventsData bucketChargeBackEventsData) {
        Date lastUpdate = DatesCalculation.setDateOnBeginningOfDay(bucketChargeBackEventsData.getLastUpdate());

        bucketChargeBackEventsData.getCurrentDateChargeBacks().forEach((hash, amount) ->
                bucketChargeBackEventsData.getOldDateChargeBacks().put(hash, lastUpdate));

        bucketChargeBackEventsData.getCurrentDatePaymentTransactions().forEach((hash, amount) ->
                bucketChargeBackEventsData.getOldDatePaymentTransactions().put(hash, lastUpdate));
    }


    private void updateBucketScoresByFunction(Map<HighFrequencyEventScore, Double> eventScoresToCalculatedScoreMap) {
        if (eventScoresToCalculatedScoreMap.containsKey(getChargeBackEventScoreByEventScoreType(HighFrequencyEventScoreType.CHARGE_BACK_AMOUNT))) {
            bucketChargeBackEventsData.setTotalContributionOfChargeBacksAndCreditsAmountContribution(
                    eventScoresToCalculatedScoreMap.get(getChargeBackEventScoreByEventScoreType(HighFrequencyEventScoreType.CHARGE_BACK_AMOUNT)));
        }
        if (eventScoresToCalculatedScoreMap.containsKey(getChargeBackEventScoreByEventScoreType(HighFrequencyEventScoreType.CHARGE_BACK_NUMBER))) {
            bucketChargeBackEventsData.setTotalContributionOfChargeBacksAndCreditsNumberContribution(
                    eventScoresToCalculatedScoreMap.get(getChargeBackEventScoreByEventScoreType(HighFrequencyEventScoreType.CHARGE_BACK_NUMBER)));
        }
    }

    private double getWeightByEventScore(HighFrequencyEventScoreType eventScoreType) {
        if (behaviorHighFrequencyEventsScore.getHighFrequencyEventScoreMap().get(eventScoreType) == null) {
            return 0;
        }
        return behaviorHighFrequencyEventsScore.getHighFrequencyEventScoreMap().get(eventScoreType).getWeight();
    }

    public double getBucketSumScore(BucketChargeBackEventsData bucketChargeBackEventsData) {
        double amountCalculationScore = bucketChargeBackEventsData.getTotalContributionOfChargeBacksAndCreditsAmountContribution()
                * getWeightByEventScore(HighFrequencyEventScoreType.CHARGE_BACK_AMOUNT);
        double numberCalculationScore = bucketChargeBackEventsData.getTotalContributionOfChargeBacksAndCreditsNumberContribution()
                * getWeightByEventScore(HighFrequencyEventScoreType.CHARGE_BACK_NUMBER);

        return amountCalculationScore + numberCalculationScore;
    }
}
