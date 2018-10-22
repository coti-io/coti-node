package io.coti.trustscore.bl.BucketCalculator;

import io.coti.trustscore.bl.Decays.DecayTransactionCalculator;
import io.coti.trustscore.bl.Decays.TransactionEventDecay;
import io.coti.trustscore.bl.Decays.VectorDecaysTransactionCalculator;
import io.coti.trustscore.bl.ScoreFunctionCalculation.FunctionTransactionCalculator;
import io.coti.trustscore.bl.ScoreFunctionCalculation.IFunctionCalculator;
import io.coti.trustscore.bl.ScoreFunctionCalculation.VectorFunctionTransactionCalculator;
import io.coti.trustscore.data.Buckets.BucketTransactionEventsData;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.rulesData.RulesData;
import io.coti.trustscore.rulesData.TransactionEventScore;
import io.coti.trustscore.rulesData.TransactionEventScoreType;
import io.coti.trustscore.rulesData.TransactionEventsScore;
import javafx.util.Pair;
import lombok.Data;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import static io.coti.trustscore.utils.DatesCalculation.addToDateByDays;
import static io.coti.trustscore.utils.DatesCalculation.calculateDaysDiffBetweenDates;
import static io.coti.trustscore.utils.DatesCalculation.setDateOnBeginningOfDay;


@Data
public class BucketTransactionsCalculator implements IBucketCalculator {

    private static Map<UserType, TransactionEventsScore> userToTransactionEventsScoreMapping;
    private BucketTransactionEventsData bucketTransactionEventsData;
    private TransactionEventsScore transactionEventsScore;


    public static void init(RulesData rulesData) {
        userToTransactionEventsScoreMapping = rulesData.getUsersRules().entrySet().stream().
                collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getTransactionEventsScore()));
    }

    public BucketTransactionsCalculator(BucketTransactionEventsData bucketTransactionEventsData){
        this.bucketTransactionEventsData = bucketTransactionEventsData;
        userToTransactionEventsScoreMapping.get(bucketTransactionEventsData.getUserType());
        transactionEventsScore = userToTransactionEventsScoreMapping.get(bucketTransactionEventsData.getUserType());
    }


    @Override
    public void decayScores() {
        if (bucketTransactionEventsData.lastUpdateBeforeToday()) {
            // first add Balances In The Gap Between Last Update To Now
            addAfterLastUpdateBalancesToBucket();
            int daysDiff = calculateDaysDiffBetweenDates(bucketTransactionEventsData.getLastUpdate(), new Date());
            decayDailyTransactionsEventScoresType(daysDiff);
            decayMonthlyTransactionsEventScoresType(daysDiff);
            bucketTransactionEventsData.setLastUpdate(new Date());
        }
    }

    public void decayDailyTransactionsEventScoresType(int daysDiff) {
        Map<TransactionEventScore, Double> transactionEventToTransactionEventScoreMap = new HashMap<>();
        transactionEventToTransactionEventScoreMap.put(getEventScoreByEventScoreType(TransactionEventScoreType.TURNOVER),
                bucketTransactionEventsData.getCurrentDateTurnOverContribution()
                        + bucketTransactionEventsData.getOldDateTurnOverContribution());

        transactionEventToTransactionEventScoreMap.put(getEventScoreByEventScoreType(TransactionEventScoreType.TRANSACTION_FREQUENCY),
                bucketTransactionEventsData.getCurrentDateNumberOfTransactionsContribution()
                        + bucketTransactionEventsData.getOldDateNumberOfTransactionsContribution());

        Map<TransactionEventScore, Double> decayedScores = new DecayTransactionCalculator(transactionEventToTransactionEventScoreMap).calculate(daysDiff);
        updateDaysEventsScoresAfterDecayed(decayedScores);
    }

    private void updateDaysEventsScoresAfterDecayed(Map<TransactionEventScore, Double> decayedScores) {

        TransactionEventsScore transactionEventsScore = userToTransactionEventsScoreMapping.get(bucketTransactionEventsData.getUserType());

        bucketTransactionEventsData.setCurrentDateNumberOfTransactions(0);
        bucketTransactionEventsData.setCurrentDateNumberOfTransactionsContribution(0);
        bucketTransactionEventsData.setOldDateNumberOfTransactionsContribution(decayedScores
                .get(transactionEventsScore.getTransactionEventScoreMap()
                        .get(TransactionEventScoreType.TRANSACTION_FREQUENCY)));

        bucketTransactionEventsData.setCurrentDateTurnOver(0);
        bucketTransactionEventsData.setCurrentDateTurnOverContribution(0);
        bucketTransactionEventsData.setOldDateTurnOverContribution(decayedScores
                .get(transactionEventsScore.getTransactionEventScoreMap()
                        .get(TransactionEventScoreType.TURNOVER)));
    }



    public void decayAndUpdateOldMonthlyEventScores(TransactionEventScore transactionEventScore,int daysDiff) {
        TransactionEventDecay transactionEventDecay = new TransactionEventDecay(transactionEventScore,bucketTransactionEventsData.getOldMonthBalanceContribution());

        bucketTransactionEventsData.setOldMonthBalanceContribution(new DecayTransactionCalculator().calculateEntry(transactionEventDecay, daysDiff)
                .getValue());
    }

    public void decayMonthlyTransactionsEventScoresType(int daysDiff) {


        // decay old tail
        decayAndUpdateOldMonthlyEventScores(getEventScoreByEventScoreType(TransactionEventScoreType.AVERAGE_BALANCE), daysDiff);

        // Map of balances after 30 days. move to the tail after decay.
        Map<Long, Pair<Double, Double>> currentMonthBalanceToTailMap = new HashMap<>();

        // Map of balances in 30 days. decay, but keep in current balance.
        Map<Long, Pair<Double, Double>> currentMonthBalanceMap = bucketTransactionEventsData.getCurrentMonthBalance();

        // moving old balances from currentMonthBalanceMap to currentMonthBalanceToTailMap.
        for (Iterator<Map.Entry<Long, Pair<Double, Double>>> currentMonthBalanceIterator
             = currentMonthBalanceMap.entrySet().iterator(); currentMonthBalanceIterator.hasNext(); ) {
            Map.Entry<Long, Pair<Double, Double>> entry = currentMonthBalanceIterator.next();
            daysDiff = calculateDaysDiffBetweenDates(new Date(entry.getKey()), new Date());
            if (daysDiff >= 30) {
                currentMonthBalanceToTailMap.put(new Date(entry.getKey()).getTime(), entry.getValue());
                currentMonthBalanceIterator.remove();
            }
        }

        Map<TransactionEventScore, Map<Long, Pair<Double, Double>>> eventScoresToDatesScoreMap = new HashMap<>();

        Date lastUpdate = bucketTransactionEventsData.getLastUpdate();

        //Decay currentMonthEventTotalNewScoresToTailMap
        eventScoresToDatesScoreMap.put(getEventScoreByEventScoreType(TransactionEventScoreType.AVERAGE_BALANCE), currentMonthBalanceToTailMap);
        Map<TransactionEventScore, Double> currentMonthEventsToTailAfterDecayedMap = new VectorDecaysTransactionCalculator(eventScoresToDatesScoreMap).calculateDatesVectorDecays(lastUpdate);

        eventScoresToDatesScoreMap.clear();

        //recalculate and Decay currentMonthEventsMap
        eventScoresToDatesScoreMap.put(getEventScoreByEventScoreType(TransactionEventScoreType.AVERAGE_BALANCE), currentMonthBalanceMap);
        Map<TransactionEventScore, Double> currentMonthEventsAfterDecayedMap = new VectorDecaysTransactionCalculator(eventScoresToDatesScoreMap).calculateDatesVectorDecays(lastUpdate);

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
        setCurrentMonthTransactionsScores();
    }

    public void setCurrentDayTransactionsScores() {

        Map<TransactionEventScore, String> transactionEventScoreToCalculationFormulaMap = new HashMap<>();
        transactionEventScoreToCalculationFormulaMap.put(transactionEventsScore.getTransactionEventScoreMap()
                .get(TransactionEventScoreType.TURNOVER), createTurnOverScoreFormula(bucketTransactionEventsData));
        transactionEventScoreToCalculationFormulaMap.put(transactionEventsScore.getTransactionEventScoreMap()
                .get(TransactionEventScoreType.TRANSACTION_FREQUENCY), createTransactionFrequencyScoreFormula());

        IFunctionCalculator functionCalculator = new FunctionTransactionCalculator(transactionEventScoreToCalculationFormulaMap);
        Map<TransactionEventScore, Double> eventScoresToFunctionalScoreMap = functionCalculator.calculate();
        updateBucketTotalScoresByFunction(bucketTransactionEventsData, eventScoresToFunctionalScoreMap);
    }

    public void setCurrentMonthTransactionsScores() {


        Map<TransactionEventScore, Map<Long, String>> eventScoresToDatesScoreFormulaMap = new HashMap<>();
        eventScoresToDatesScoreFormulaMap.put(transactionEventsScore.getTransactionEventScoreMap()
                .get(TransactionEventScoreType.AVERAGE_BALANCE), createLastDaysAverageBalanceScoreFormula());
        // Calculate every day from the last days balance score.
        VectorFunctionTransactionCalculator vectorFunctionTransactionCalculator = new VectorFunctionTransactionCalculator(eventScoresToDatesScoreFormulaMap);
        Map<TransactionEventScore, Map<Long, Double>> latestTransactionEventScoreToCalculationFormulaMap =
                (vectorFunctionTransactionCalculator).calculateVectorFunction();
        Map<Long, Double> latestBalanceTransactionEventScoreToCalculationFormulaMap =
                latestTransactionEventScoreToCalculationFormulaMap.get(transactionEventsScore.getTransactionEventScoreMap()
                        .get(TransactionEventScoreType.AVERAGE_BALANCE));
        updateCurrentMonthBalance(bucketTransactionEventsData,latestBalanceTransactionEventScoreToCalculationFormulaMap);
        updateCurrentMonthBalanceContribution();
    }

    public void addAfterLastUpdateBalancesToBucket() {

        Map<Long, Pair<Double, Double>> currentMonthBalanceMap = bucketTransactionEventsData.getCurrentMonthBalance();
        long beginningOfToday = setDateOnBeginningOfDay(new Date()).getTime();
        if (currentMonthBalanceMap.size() > 0) {
            long lastDayWithChangeInBalance = currentMonthBalanceMap.keySet().stream()
                    .reduce((i, j) -> i > j ? i : j).get();
            double previousBalance = currentMonthBalanceMap.get(lastDayWithChangeInBalance).getKey();
            double previousBalanceScore = currentMonthBalanceMap.get(lastDayWithChangeInBalance).getValue();
            int i = 1;
            for (long day = addToDateByDays(lastDayWithChangeInBalance, 1).getTime();
                 day <= beginningOfToday;
                 day = addToDateByDays(day, 1).getTime(), i++) {
                double balanceDayScore = new DecayTransactionCalculator().calculateEntry(new TransactionEventDecay(transactionEventsScore
                        .getTransactionEventScoreMap()
                        .get(TransactionEventScoreType.AVERAGE_BALANCE), previousBalanceScore), -i)
                        .getValue();
                currentMonthBalanceMap.put(day, new Pair<>(previousBalance, balanceDayScore));
            }
        }
    }



    public Map<Long, String> createLastDaysAverageBalanceScoreFormula() {

        String nonlinearFunctionString = transactionEventsScore.getTransactionEventScoreMap().get(TransactionEventScoreType.AVERAGE_BALANCE).getNonlinearFunction();
        Map<Long, Pair<Double, Double>> currentMonthBalanceByDayMap = bucketTransactionEventsData.getCurrentMonthBalance();
        return currentMonthBalanceByDayMap.entrySet().stream()
                .filter(x -> x.getValue().getValue() == 0)
                .collect(Collectors.toMap(e -> e.getKey(), e -> nonlinearFunctionString.replace("B", String.valueOf(e.getValue().getKey()))));//.concat(
    }

    private void updateCurrentMonthBalanceContribution() {
        double sumCurrentMonthBalanceContribution = 0.0;
        Map<Long, Pair<Double, Double>> currentMonthBalance = bucketTransactionEventsData.getCurrentMonthBalance();
        for (Map.Entry<Long, Pair<Double, Double>> currentMonthBalanceEntry : currentMonthBalance.entrySet()) {
            sumCurrentMonthBalanceContribution += currentMonthBalanceEntry.getValue().getValue();
        }
        bucketTransactionEventsData.setCurrentMonthBalanceContribution(sumCurrentMonthBalanceContribution);
    }

    private void updateCurrentMonthBalance(BucketTransactionEventsData bucketTransactionEventsData, Map<Long, Double> dayToScoreMap) {
        for (Map.Entry<Long, Double> dayToScoreMapEntry : dayToScoreMap.entrySet()) {
            double dayleBalance = bucketTransactionEventsData.getCurrentMonthBalance().get(dayToScoreMapEntry.getKey()).getKey();
            double dayleBalanceContribution = dayToScoreMapEntry.getValue();
            bucketTransactionEventsData.getCurrentMonthBalance().put(dayToScoreMapEntry.getKey(), new Pair<>(dayleBalance, dayleBalanceContribution));
        }
    }


    private double getWeightByEventScore(String eventScoreType) {
        return transactionEventsScore.getTransactionEventScoreMap().get(eventScoreType).getWeight();
    }

    private TransactionEventScore getEventScoreByEventScoreType(String eventScoreType) {
        return transactionEventsScore.getTransactionEventScoreMap().get(eventScoreType);
    }

    private void updateBucketTotalScoresByFunction(BucketTransactionEventsData bucketTransactionEventsData, Map<TransactionEventScore, Double> transactionEventScoreToUpdatedBucketValuesMap) {
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
        String nonlinearFunctionString = transactionEventsScore.getTransactionEventScoreMap().get(TransactionEventScoreType.TRANSACTION_FREQUENCY).getNonlinearFunction();

        double numberOfTransactions = bucketTransactionEventsData.getCurrentDateNumberOfTransactions();
        return nonlinearFunctionString.replace("N", String.valueOf(numberOfTransactions));
    }

    public Map<Long, String> createAverageBalanceScoreFormula() {
        String nonlinearFunctionString = transactionEventsScore.getTransactionEventScoreMap().get(TransactionEventScoreType.AVERAGE_BALANCE).getNonlinearFunction();
        Map<Long, Pair<Double, Double>> currentMonthBalanceByDayMap = bucketTransactionEventsData.getCurrentMonthBalance();
        return currentMonthBalanceByDayMap.entrySet().stream().
                collect(Collectors.toMap(e -> e.getKey(), e -> nonlinearFunctionString.replace("B", String.valueOf(e.getValue().getKey()))));
    }

    public String createDailyBalanceScoreFormula() {
        String nonlinearFunctionString = transactionEventsScore.getTransactionEventScoreMap().get(TransactionEventScoreType.AVERAGE_BALANCE).getNonlinearFunction();
        double numberOfBalance = bucketTransactionEventsData.getCurrentMonthBalance().get(setDateOnBeginningOfDay(new Date()).getTime()).getKey();
        return nonlinearFunctionString.replace("B", String.valueOf(numberOfBalance));
    }

    public String createTurnOverScoreFormula(BucketTransactionEventsData bucketTransactionEventsData) {
        String nonlinearFunctionString = transactionEventsScore.getTransactionEventScoreMap().get(TransactionEventScoreType.TURNOVER).getNonlinearFunction();
        double turnover = bucketTransactionEventsData.getCurrentDateTurnOver();
        return nonlinearFunctionString.replace("T", String.valueOf(turnover));
    }

    public double getBucketSumScore(BucketTransactionEventsData bucketTransactionEventsData) {
        return ((bucketTransactionEventsData.getCurrentDateTurnOverContribution() + bucketTransactionEventsData.getOldDateTurnOverContribution()) * getWeightByEventScore(TransactionEventScoreType.TURNOVER))
                + ((bucketTransactionEventsData.getCurrentMonthBalanceContribution() + bucketTransactionEventsData.getOldMonthBalanceContribution()) * getWeightByEventScore(TransactionEventScoreType.AVERAGE_BALANCE))
                + ((bucketTransactionEventsData.getCurrentDateNumberOfTransactionsContribution() + bucketTransactionEventsData.getOldDateNumberOfTransactionsContribution()) * getWeightByEventScore(TransactionEventScoreType.TRANSACTION_FREQUENCY));
    }
}
