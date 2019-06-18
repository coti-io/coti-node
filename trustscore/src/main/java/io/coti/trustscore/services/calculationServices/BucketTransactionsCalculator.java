package io.coti.trustscore.services.calculationservices;

import io.coti.trustscore.config.rules.RulesData;
import io.coti.trustscore.config.rules.TransactionEventScore;
import io.coti.trustscore.config.rules.TransactionEventsScore;
import io.coti.trustscore.data.Buckets.BucketEventData;
import io.coti.trustscore.data.Buckets.BucketTransactionEventsData;
import io.coti.trustscore.data.Enums.TransactionEventScoreType;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.BalanceCountAndContribution;
import io.coti.trustscore.services.calculationservices.interfaces.IScoreCalculator;
import io.coti.trustscore.utils.DatesCalculation;
import javafx.util.Pair;
import lombok.Data;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Data
public class BucketTransactionsCalculator extends BucketCalculator {

    private static final int MONTH_LENGTH = 30;
    private static Map<UserType, TransactionEventsScore> userToTransactionEventsScoreMapping;
    private BucketTransactionEventsData bucketTransactionEventsData;
    private TransactionEventsScore transactionEventsScore;


    public BucketTransactionsCalculator(BucketTransactionEventsData bucketTransactionEventsData) {
        this.bucketTransactionEventsData = bucketTransactionEventsData;
        transactionEventsScore = userToTransactionEventsScoreMapping.get(bucketTransactionEventsData.getUserType());
    }

    public static void init(RulesData rulesData) {
        userToTransactionEventsScoreMapping = rulesData.getUserTypeToUserScoreMap().entrySet().stream().
                collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getTransactionEventScore()));
    }


    public boolean decayScores(BucketEventData bucketTransactionEventsData) {
        if (!bucketTransactionEventsData.lastUpdateBeforeToday()) {
            return false;
        }

        int daysDiff = DatesCalculation.calculateDaysDiffBetweenDates(DatesCalculation.setDateOnBeginningOfDay(bucketTransactionEventsData.getLastUpdate()),
                DatesCalculation.setDateOnBeginningOfDay(new Date()));
        decayDailyTransactionsEventScoresType(daysDiff);

        // add Balances In The Gap Between Last Update To Now
        if (userToTransactionEventsScoreMapping.get(bucketTransactionEventsData.getUserType())
                .getTransactionEventScoreMap().containsKey(TransactionEventScoreType.AVERAGE_BALANCE)) {
            addBalancesInTheGapBetweenLastUpdateToNow();
            decayMonthlyTransactionsEventScoresType(daysDiff);
        }
        bucketTransactionEventsData.setLastUpdate(DatesCalculation.setDateOnBeginningOfDay(new Date()));

        return true;
    }

    public void decayDailyTransactionsEventScoresType(int daysDiff) {
        Map<TransactionEventScore, Double> transactionEventScoreToScoreMap = new ConcurrentHashMap<>();
        transactionEventScoreToScoreMap.put(getEventScoreByEventScoreType(TransactionEventScoreType.TURNOVER),
                bucketTransactionEventsData.getCurrentDateTurnOverContribution()
                        + bucketTransactionEventsData.getOldDateTurnOverContribution());

        transactionEventScoreToScoreMap.put(getEventScoreByEventScoreType(TransactionEventScoreType.TRANSACTION_FREQUENCY),
                bucketTransactionEventsData.getCurrentDateNumberOfTransactionsContribution()
                        + bucketTransactionEventsData.getOldDateNumberOfTransactionsContribution());

        Map<TransactionEventScore, Double> transactionEventScoreToDecayedScores = new DecayCalculator(transactionEventScoreToScoreMap).calculate(daysDiff);
        updateDaysEventsScoresAfterDecayed(transactionEventScoreToDecayedScores);
    }

    private void updateDaysEventsScoresAfterDecayed(Map<TransactionEventScore, Double> transactionEventScoreToDecayedScores) {

        TransactionEventsScore transactionEventsScore = userToTransactionEventsScoreMapping.get(bucketTransactionEventsData.getUserType());

        bucketTransactionEventsData.setCurrentDateNumberOfTransactions(0);
        bucketTransactionEventsData.setCurrentDateNumberOfTransactionsContribution(0);
        bucketTransactionEventsData.setOldDateNumberOfTransactionsContribution(transactionEventScoreToDecayedScores
                .get(transactionEventsScore.getTransactionEventScoreMap()
                        .get(TransactionEventScoreType.TRANSACTION_FREQUENCY)));

        bucketTransactionEventsData.setCurrentDateTurnOver(0);
        bucketTransactionEventsData.setCurrentDateTurnOverContribution(0);
        bucketTransactionEventsData.setOldDateTurnOverContribution(transactionEventScoreToDecayedScores
                .get(transactionEventsScore.getTransactionEventScoreMap()
                        .get(TransactionEventScoreType.TURNOVER)));
    }


    public void decayAndUpdateOldMonthlyEventScores(TransactionEventScore transactionEventScore, int daysDiff) {
        EventDecay transactionEventDecay = new EventDecay(transactionEventScore, bucketTransactionEventsData.getOldMonthBalanceContribution());
        Pair<TransactionEventScore, Double> scoreAfterDecay = new DecayCalculator().calculateEntry(transactionEventDecay, daysDiff);
        bucketTransactionEventsData.setOldMonthBalanceContribution(scoreAfterDecay.getValue());
    }

    public Map<Date, BalanceCountAndContribution> getCurrentMonthDayToMoveToTailBalance(int daysDiff) {
        // Map of balances after 30 days. will be moved to the tail after decay.
        Map<Date, BalanceCountAndContribution> currentMonthDayToTailBalanceMap = new ConcurrentHashMap<>();

        // Map of balances in 30 days. decay, but keep in current balance.
        Map<Date, BalanceCountAndContribution> currentMonthBalanceMap
                = bucketTransactionEventsData.getCurrentMonthDayToBalanceCountAndContribution();

        // moving old balances from currentMonthBalanceMap to currentMonthBalanceToTailMap.
        for (Iterator<Map.Entry<Date, BalanceCountAndContribution>> currentMonthBalanceIterator
             = currentMonthBalanceMap.entrySet().iterator(); currentMonthBalanceIterator.hasNext(); ) {
            Map.Entry<Date, BalanceCountAndContribution> entry = currentMonthBalanceIterator.next();
            daysDiff = DatesCalculation.calculateDaysDiffBetweenDates(DatesCalculation.setDateOnBeginningOfDay(entry.getKey()),
                    DatesCalculation.setDateOnBeginningOfDay(new Date()));
            if (daysDiff >= MONTH_LENGTH) {
                currentMonthDayToTailBalanceMap.put(entry.getKey(), entry.getValue());
                currentMonthBalanceIterator.remove();
            }
        }
        return currentMonthDayToTailBalanceMap;
    }

    public void decayMonthlyTransactionsEventScoresType(int daysDiff) {

        // decay old tail
        decayAndUpdateOldMonthlyEventScores(getEventScoreByEventScoreType(TransactionEventScoreType.AVERAGE_BALANCE), daysDiff);

        // Map of balances after 30 days. will be moved to the tail after decay.
        Map<Date, BalanceCountAndContribution> currentMonthDayToTailBalanceMap = getCurrentMonthDayToMoveToTailBalance(daysDiff);

        Map<TransactionEventScore, Map<Date, BalanceCountAndContribution>> eventScoresToDatesScoreMap = new ConcurrentHashMap<>();
        Date lastUpdate = bucketTransactionEventsData.getLastUpdate();

        //Decay currentMonthEventTotalNewScoresToTailMap
        eventScoresToDatesScoreMap.put(getEventScoreByEventScoreType(TransactionEventScoreType.AVERAGE_BALANCE), currentMonthDayToTailBalanceMap);
        Map<TransactionEventScore, Double> currentMonthEventsToTailAfterDecayedMap = new VectorDecaysCalculator(eventScoresToDatesScoreMap).calculateDatesVectorDecays(lastUpdate);

        eventScoresToDatesScoreMap.clear();

        //recalculate and Decay currentMonthEventsMap
        eventScoresToDatesScoreMap.put(getEventScoreByEventScoreType(TransactionEventScoreType.AVERAGE_BALANCE),
                bucketTransactionEventsData.getCurrentMonthDayToBalanceCountAndContribution());
        Map<TransactionEventScore, Double> currentMonthEventsAfterDecayedMap = new VectorDecaysCalculator(eventScoresToDatesScoreMap).calculateDatesVectorDecays(lastUpdate);

        updateMonthEventsScoresAfterDecayed(currentMonthEventsToTailAfterDecayedMap, currentMonthEventsAfterDecayedMap);
    }

    private void updateMonthEventsScoresAfterDecayed(Map<TransactionEventScore, Double> currentMonthEventsToTailAfterDecayedMap,
                                                     Map<TransactionEventScore, Double> currentMonthEventsAfterDecayedMap) {


        bucketTransactionEventsData.setOldMonthBalanceContribution(bucketTransactionEventsData.getOldMonthBalanceContribution()
                + currentMonthEventsToTailAfterDecayedMap.get(transactionEventsScore.getTransactionEventScoreMap()
                .get(TransactionEventScoreType.AVERAGE_BALANCE)));

        bucketTransactionEventsData.setCurrentMonthBalanceContribution(currentMonthEventsAfterDecayedMap
                .get(transactionEventsScore.getTransactionEventScoreMap()
                        .get(TransactionEventScoreType.AVERAGE_BALANCE)));
    }

    @Override
    public void setCurrentScores() {
        setCurrentDayTransactionsScores();
        if (userToTransactionEventsScoreMapping.get(bucketTransactionEventsData.getUserType())
                .getTransactionEventScoreMap().containsKey(TransactionEventScoreType.AVERAGE_BALANCE)) {
            setCurrentMonthTransactionsScores();
        }
    }

    public void setCurrentDayTransactionsScores() {

        Map<TransactionEventScore, String> transactionEventScoreToCalculationFormulaMap = new ConcurrentHashMap<>();
        transactionEventScoreToCalculationFormulaMap.put(transactionEventsScore.getTransactionEventScoreMap()
                .get(TransactionEventScoreType.TURNOVER), createTurnOverScoreFormula(bucketTransactionEventsData));
        transactionEventScoreToCalculationFormulaMap.put(transactionEventsScore.getTransactionEventScoreMap()
                .get(TransactionEventScoreType.TRANSACTION_FREQUENCY), createTransactionFrequencyScoreFormula());

        IScoreCalculator functionCalculator = new ScoreCalculator(transactionEventScoreToCalculationFormulaMap);
        Map<TransactionEventScore, Double> eventScoresToFunctionalScoreMap = functionCalculator.calculate();
        updateBucketScoresByFunction(eventScoresToFunctionalScoreMap);
    }

    public void setCurrentMonthTransactionsScores() {
        Map<TransactionEventScore, Map<Date, String>> eventScoresToDatesScoreFormulaMap = new ConcurrentHashMap<>();
        eventScoresToDatesScoreFormulaMap.put(transactionEventsScore.getTransactionEventScoreMap()
                .get(TransactionEventScoreType.AVERAGE_BALANCE), createLastDaysAverageBalanceScoreFormula());
        // Calculate every day from the last days balance score.
        VectorScoreCalculator vectorScoreCalculator = new VectorScoreCalculator(eventScoresToDatesScoreFormulaMap);
        Map<TransactionEventScore, Map<Date, Double>> latestTransactionEventScoreToCalculationFormulaMap =
                (vectorScoreCalculator).calculateVectorScore();
        Map<Date, Double> latestBalanceTransactionEventScoreToCalculationFormulaMap =
                latestTransactionEventScoreToCalculationFormulaMap.get(transactionEventsScore.getTransactionEventScoreMap()
                        .get(TransactionEventScoreType.AVERAGE_BALANCE));
        updateCurrentMonthBalance(bucketTransactionEventsData, latestBalanceTransactionEventScoreToCalculationFormulaMap);
        updateCurrentMonthBalanceContribution();
    }

    public void addBalancesInTheGapBetweenLastUpdateToNow() {

        Map<Date, BalanceCountAndContribution> currentMonthBalanceMap
                = bucketTransactionEventsData.getCurrentMonthDayToBalanceCountAndContribution();
        long beginningOfToday = DatesCalculation.setDateOnBeginningOfDay(new Date()).getTime();
        if (!currentMonthBalanceMap.isEmpty()) {

            Date lastDayWithChangeInBalance = Collections.max(currentMonthBalanceMap.keySet());
            double previousBalance = currentMonthBalanceMap.get(lastDayWithChangeInBalance).getCount();
            double previousBalanceScore = currentMonthBalanceMap.get(lastDayWithChangeInBalance).getContribution();
            int numberOfDecays = 1;
            // Iterate on days between last update day till today
            for (long day = DatesCalculation.addToDateByDays(lastDayWithChangeInBalance.getTime(), 1).getTime();
                 day <= beginningOfToday;
                 day = DatesCalculation.addToDateByDays(day, 1).getTime(), numberOfDecays++) {
                Pair<TransactionEventScore, Double> scoreAfterDecay = new DecayCalculator().calculateEntry(new EventDecay(transactionEventsScore
                        .getTransactionEventScoreMap()
                        .get(TransactionEventScoreType.AVERAGE_BALANCE), previousBalanceScore), -numberOfDecays);
                double balanceDayScore = scoreAfterDecay.getValue();
                currentMonthBalanceMap.put(new Date(day), new BalanceCountAndContribution(previousBalance, balanceDayScore));
            }
        }
    }


    public Map<Date, String> createLastDaysAverageBalanceScoreFormula() {

        String nonlinearFormula = transactionEventsScore.getTransactionEventScoreMap()
                .get(TransactionEventScoreType.AVERAGE_BALANCE).getNonlinearFunction();
        Map<Date, BalanceCountAndContribution> currentMonthBalanceByDayMap
                = bucketTransactionEventsData.getCurrentMonthDayToBalanceCountAndContribution();
        return currentMonthBalanceByDayMap.entrySet().stream()
                .filter(x -> x.getValue().getContribution() == 0)
                .collect(Collectors.toMap(e -> e.getKey(), e -> nonlinearFormula.replace("B", String.valueOf(e.getValue().getCount()))));//.concat(
    }

    private void updateCurrentMonthBalanceContribution() {
        double sumCurrentMonthBalanceContribution = 0;
        Map<Date, BalanceCountAndContribution> currentMonthBalance
                = bucketTransactionEventsData.getCurrentMonthDayToBalanceCountAndContribution();
        for (Map.Entry<Date, BalanceCountAndContribution> currentMonthBalanceEntry : currentMonthBalance.entrySet()) {
            sumCurrentMonthBalanceContribution += currentMonthBalanceEntry.getValue().getContribution();
        }
        bucketTransactionEventsData.setCurrentMonthBalanceContribution(sumCurrentMonthBalanceContribution);
    }

    private void updateCurrentMonthBalance(BucketTransactionEventsData bucketTransactionEventsData, Map<Date, Double> dayToScoreMap) {
        for (Map.Entry<Date, Double> dayToScoreMapEntry : dayToScoreMap.entrySet()) {
            double dailyBalance = bucketTransactionEventsData.getCurrentMonthDayToBalanceCountAndContribution()
                    .get(dayToScoreMapEntry.getKey()).getCount();
            double dailyBalanceContribution = dayToScoreMapEntry.getValue();
            bucketTransactionEventsData.getCurrentMonthDayToBalanceCountAndContribution()
                    .put(dayToScoreMapEntry.getKey(), new BalanceCountAndContribution(dailyBalance, dailyBalanceContribution));
        }
    }


    private double getWeightByEventScore(TransactionEventScoreType eventScoreType) {
        if (transactionEventsScore.getTransactionEventScoreMap().get(eventScoreType) == null) {
            return 0;
        }
        return transactionEventsScore.getTransactionEventScoreMap().get(eventScoreType).getWeight();
    }

    private TransactionEventScore getEventScoreByEventScoreType(TransactionEventScoreType eventScoreType) {
        return transactionEventsScore.getTransactionEventScoreMap().get(eventScoreType);
    }

    private void updateBucketScoresByFunction(Map<TransactionEventScore, Double> transactionEventScoreToUpdatedBucketValuesMap) {

        if (transactionEventScoreToUpdatedBucketValuesMap.containsKey(getEventScoreByEventScoreType(TransactionEventScoreType.TURNOVER))) {
            bucketTransactionEventsData.setCurrentDateTurnOverContribution(
                    transactionEventScoreToUpdatedBucketValuesMap.get(getEventScoreByEventScoreType(TransactionEventScoreType.TURNOVER)));
        }
        if (transactionEventScoreToUpdatedBucketValuesMap.containsKey(getEventScoreByEventScoreType(TransactionEventScoreType.AVERAGE_BALANCE))) {
            bucketTransactionEventsData.setCurrentMonthBalanceContribution(
                    transactionEventScoreToUpdatedBucketValuesMap.get(getEventScoreByEventScoreType(TransactionEventScoreType.AVERAGE_BALANCE)));
        }
        if (transactionEventScoreToUpdatedBucketValuesMap.containsKey(getEventScoreByEventScoreType(TransactionEventScoreType.TRANSACTION_FREQUENCY))) {
            bucketTransactionEventsData.setCurrentDateNumberOfTransactionsContribution(
                    transactionEventScoreToUpdatedBucketValuesMap.get(getEventScoreByEventScoreType(TransactionEventScoreType.TRANSACTION_FREQUENCY)));
        }
    }


    public String createTransactionFrequencyScoreFormula() {
        String nonlinearFunctionformulaString = transactionEventsScore.getTransactionEventScoreMap()
                .get(TransactionEventScoreType.TRANSACTION_FREQUENCY).getNonlinearFunction();

        double numberOfTransactions = bucketTransactionEventsData.getCurrentDateNumberOfTransactions();
        return nonlinearFunctionformulaString.replace("N", String.valueOf(numberOfTransactions));
    }


    public String createTurnOverScoreFormula(BucketTransactionEventsData bucketTransactionEventsData) {
        String nonlinearFunctionString = transactionEventsScore.getTransactionEventScoreMap().get(TransactionEventScoreType.TURNOVER).getNonlinearFunction();
        double turnover = bucketTransactionEventsData.getCurrentDateTurnOver();
        return nonlinearFunctionString.replace("T", String.valueOf(turnover));
    }

    public double getBucketSumScore(BucketTransactionEventsData bucketTransactionEventsData) {
        return ((bucketTransactionEventsData.getCurrentDateTurnOverContribution()
                + bucketTransactionEventsData.getOldDateTurnOverContribution()) * getWeightByEventScore(TransactionEventScoreType.TURNOVER))
//                + ((bucketTransactionEventsData.getCurrentMonthBalanceContribution()
//                + bucketTransactionEventsData.getOldMonthBalanceContribution()) * getWeightByEventScore(TransactionEventScoreType.AVERAGE_BALANCE))
                + ((bucketTransactionEventsData.getCurrentDateNumberOfTransactionsContribution()
                + bucketTransactionEventsData.getOldDateNumberOfTransactionsContribution()) * getWeightByEventScore(TransactionEventScoreType.TRANSACTION_FREQUENCY));
    }
}
