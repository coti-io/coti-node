package io.coti.trustscore.bl.BucketCalculator;

import io.coti.basenode.data.Hash;
import io.coti.trustscore.bl.Decays.DecayTransactionCalculator;
import io.coti.trustscore.bl.Decays.VectorDecaysTransactionCalculator;
import io.coti.trustscore.bl.FunctionCalculator;
import io.coti.trustscore.bl.ScoreFunctionCalculation.FunctionTransactionCalculator;
import io.coti.trustscore.bl.ScoreFunctionCalculation.VectorFunctionTransactionCalculator;
import io.coti.trustscore.data.Buckets.BucketTransactionEventsData;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.rulesData.*;
import io.coti.trustscore.model.TrustScores;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static io.coti.trustscore.utils.DatesCalculation.calculateDaysdiffBetweenDates;

@Service
@Data
public class BucketTransactionsCalculator {
    @Autowired
    private TrustScores trustScores;

    private TransactionEventsScore transactionEventsScore;
    private Map<UserType, TransactionEventsScore> userToTransactionEventsScoreMapping;
    private BucketTransactionEventsData bucketTransactionEventsData;

    public void init(RulesData rulesData) {
        userToTransactionEventsScoreMapping = rulesData.getUsersRules().entrySet().stream().
                collect(Collectors.toMap(e->e.getKey(), e->e.getValue().getTransactionEventsScore()));

    }

//    public BucketTransactionsCalculator(RulesData rulesData, BucketTransactionEventsData bucketTransactionEventsData) {
//        this(rulesData);
//        setBucketTransactionEventsData(bucketTransactionEventsData);
//    }
//
//    public BucketTransactionsCalculator(RulesData rulesData) {
//        userToTransactionEventsScoreMapping = new HashMap<>();
//        for (Map.Entry<UserType, UserScoresByType> entry : rulesData.getUsersRules().entrySet()) {
//            userToTransactionEventsScoreMapping.put(entry.getKey(), entry.getValue().getTransactionEventsScore());
//        }
//    }

    public void setBucketTransactionEventsData(BucketTransactionEventsData bucketTransactionEventsData) {
        this.bucketTransactionEventsData = bucketTransactionEventsData;
        this.transactionEventsScore = userToTransactionEventsScoreMapping.get(bucketTransactionEventsData.getUserType());
    }


    public void decayTransactionScores(Hash userHash) {
        if (bucketTransactionEventsData.lastUpdateBeforeToday()) {
            int daysDiff = calculateDaysdiffBetweenDates(bucketTransactionEventsData.getLastUpdate(), new Date());
            decayDailyTransactionsEventScoresType(daysDiff);
            decayMonthlyTransactionsEventScoresType();
            bucketTransactionEventsData.setLastUpdate(new Date());
            trustScores.getByHash(userHash).setTrustScore(getBucketSumScore(bucketTransactionEventsData));
        }
    }

    public void decayMonthlyTransactionsEventScoresType() {
        Map<TransactionEventScore, Map<Date, Double>> eventScoresToDatesScoreMap = new HashMap<>();
        Map<Date, Double> currentMonthBalanceToTail = new HashMap<>();
        Map<Date, Double> currentMonthBalance = bucketTransactionEventsData.getCurrentMonthBalance();


        for (Iterator<Map.Entry<Date, Double>> currentMonthBalanceIterator
             = currentMonthBalance.entrySet().iterator(); currentMonthBalanceIterator.hasNext(); ) {
            Map.Entry<Date, Double> entry = currentMonthBalanceIterator.next();
            int daysDiff = calculateDaysdiffBetweenDates(entry.getKey(), new Date());
            if (daysDiff >= 30) {
                currentMonthBalanceToTail.put(entry.getKey(), entry.getValue());
                currentMonthBalanceIterator.remove();
            }
        }

        eventScoresToDatesScoreMap.put(getEventScoreByEventScoreType(TransactionEventScoreType.AVERAGE_BALANCE), currentMonthBalanceToTail);
        updateMonthEventsScoresAfterDecayed(new VectorDecaysTransactionCalculator(eventScoresToDatesScoreMap).calculateDatesVector());
    }

    private void updateMonthEventsScoresAfterDecayed(Map<TransactionEventScore, Double> decayedScores) {
        bucketTransactionEventsData.setCurrentMonthBalanceContribution(bucketTransactionEventsData.getCurrentMonthBalanceContribution()
                + decayedScores.get(transactionEventsScore.getTransactionEventScoreMap()
                .get(TransactionEventScoreType.AVERAGE_BALANCE)));
    }

    public void decayDailyTransactionsEventScoresType(int daysDiff) {
        Map<TransactionEventScore, Double> transactionEventToTransactionEventScoreMap = new HashMap(){{
            put(getEventScoreByEventScoreType(TransactionEventScoreType.TURNOVER),
                    bucketTransactionEventsData.getCurrentDateTurnOverContribution() + bucketTransactionEventsData.getCurrentDateTurnOverContribution());
            put(getEventScoreByEventScoreType(TransactionEventScoreType.TRANSACTION_FREQUENCY),
                    bucketTransactionEventsData.getCurrentDateNumberOfTransactionsContribution());
        }};
        Map<TransactionEventScore, Double> decayedScores = new DecayTransactionCalculator(transactionEventToTransactionEventScoreMap).calculate(daysDiff);
        updateDaysEventsScoresAfterDecayed(decayedScores);
    }

    private void updateDaysEventsScoresAfterDecayed(Map<TransactionEventScore, Double> decayedScores) {
        bucketTransactionEventsData.setCurrentDateNumberOfTransactions(0);
        bucketTransactionEventsData.setCurrentDateNumberOfTransactionsContribution(decayedScores
                .get(transactionEventsScore.getTransactionEventScoreMap()
                        .get(TransactionEventScoreType.TRANSACTION_FREQUENCY)));

        bucketTransactionEventsData.setCurrentDateTurnOver(0);
        bucketTransactionEventsData.setCurrentDateTurnOverContribution(decayedScores
                .get(transactionEventsScore.getTransactionEventScoreMap()
                        .get(TransactionEventScoreType.TURNOVER)));
    }


    public void setCurrentTransactionsScores() {
        setCurrentDayTransactionsScores();
        // updateBucketScoresByFunction(functionCalculator.calculate());
        setCurrentMonthTransactionsScores();
        updateBucketScoreByWeight();
    }

    public void setCurrentDayTransactionsScores() {
        Map<TransactionEventScore, String> transactionEventToScoreMap = new HashMap<>();
        transactionEventToScoreMap.put(transactionEventsScore.getTransactionEventScoreMap()
                .get(TransactionEventScoreType.TURNOVER), createTurnOverScoreFormula());
//        transactionEventToScoreMap.put(transactionEventsScore.getTransactionEventScoreMap()
//                .get(TransactionEventScoreType.AVERAGE_BALANCE), createAverageBalanceScoreFormula());
        transactionEventToScoreMap.put(transactionEventsScore.getTransactionEventScoreMap()
                .get(TransactionEventScoreType.TRANSACTION_FREQUENCY), createTransactionFrequencyScoreFormula());

        FunctionCalculator functionCalculator = new FunctionTransactionCalculator(transactionEventToScoreMap);
        Map<TransactionEventScore, Double> eventScoresToFunctionalScoreMap = functionCalculator.calculate();
        updateBucketScoresByFunction(eventScoresToFunctionalScoreMap);
    }

    public void setCurrentMonthTransactionsScores() {
        Map<TransactionEventScore, Map<Date, String>> transactionEventToScoreFormulaByDayMap = new HashMap<>();
        transactionEventToScoreFormulaByDayMap.put(transactionEventsScore.getTransactionEventScoreMap()
                .get(TransactionEventScoreType.AVERAGE_BALANCE), createAverageBalanceScoreFormula());

        VectorFunctionTransactionCalculator vectorFunctionTransactionCalculator = new VectorFunctionTransactionCalculator(transactionEventToScoreFormulaByDayMap);
        Map<TransactionEventScore, Map<Date, Double>> eventScoresToFunctionalScoreMap = vectorFunctionTransactionCalculator.calculateVectorFunction();
        Map<TransactionEventScore, Double> scoredSumByEventType = sumScoresByEventType(eventScoresToFunctionalScoreMap);
        updateBucketScoresByFunction(scoredSumByEventType);
    }

    private Map<TransactionEventScore, Double> sumScoresByEventType(Map<TransactionEventScore, Map<Date, Double>> eventScoresToFunctionalScoreMap) {
        Map<TransactionEventScore, Double> scoredSumByDay = new HashMap<>();
        eventScoresToFunctionalScoreMap.forEach((eventScore, scoreByDay) ->
            scoreByDay.forEach((day, score) -> {
                if (scoredSumByDay.containsKey(eventScore)) {
                    scoredSumByDay.put(eventScore, scoredSumByDay.get(eventScore) + score);
                } else {
                    scoredSumByDay.put(eventScore, score);
                }
            })
        );
        return scoredSumByDay;
    }

    private void updateBucketScoreByWeight() {
        bucketTransactionEventsData.setCurrentDateTurnOverContribution(bucketTransactionEventsData.getCurrentDateTurnOverContribution() *
                getWeightByEventScore(TransactionEventScoreType.TURNOVER));
        bucketTransactionEventsData.setCurrentMonthBalanceContribution(bucketTransactionEventsData.getCurrentMonthBalanceContribution() *
                getWeightByEventScore(TransactionEventScoreType.AVERAGE_BALANCE));
        bucketTransactionEventsData.setCurrentMonthBalanceContribution(bucketTransactionEventsData.getCurrentMonthBalanceContribution() *
                getWeightByEventScore(TransactionEventScoreType.TRANSACTION_FREQUENCY));
    }

    private double getWeightByEventScore(TransactionEventScoreType eventScoreType){
        return transactionEventsScore.getTransactionEventScoreMap().get(eventScoreType).getWeight();
    }

    private TransactionEventScore getEventScoreByEventScoreType(TransactionEventScoreType eventScoreType){
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
            bucketTransactionEventsData.setCurrentMonthBalanceContribution(
                    transactionEventScoreToUpdatedBucketValuesMap.get(getEventScoreByEventScoreType(TransactionEventScoreType.TRANSACTION_FREQUENCY)));
        }
    }


    public String createTransactionFrequencyScoreFormula() {
        String nonlinearFunctionString = transactionEventsScore.getTransactionEventScoreMap().get(TransactionEventScoreType.TRANSACTION_FREQUENCY).getNonlinearFunction();
        double numberOfTransactions = bucketTransactionEventsData.getCurrentDateNumberOfTransactions();
        return nonlinearFunctionString.replace("N", String.valueOf(numberOfTransactions));
    }

    public Map<Date, String> createAverageBalanceScoreFormula() {
        String nonlinearFunctionString = transactionEventsScore.getTransactionEventScoreMap().get(TransactionEventScoreType.AVERAGE_BALANCE).getNonlinearFunction();
        Map<Date, Double> currentMonthBalanceByDayMap = bucketTransactionEventsData.getCurrentMonthBalance();
        return currentMonthBalanceByDayMap.entrySet().stream().
                collect(Collectors.toMap(e->e.getKey(),e->nonlinearFunctionString.replace("B", String.valueOf(e.getValue()))));
    }

    public String createTurnOverScoreFormula() {
        String nonlinearFunctionString = transactionEventsScore.getTransactionEventScoreMap().get(TransactionEventScoreType.TURNOVER).getNonlinearFunction();
        double turnover = bucketTransactionEventsData.getCurrentDateTurnOver();
        return nonlinearFunctionString.replace("T", String.valueOf(turnover));
    }

    public double getBucketSumScore(BucketTransactionEventsData bucketTransactionEventsData) {
        return bucketTransactionEventsData.getCurrentDateTurnOverContribution() + bucketTransactionEventsData.getCurrentMonthBalanceContribution() + bucketTransactionEventsData.getCurrentDateNumberOfTransactionsContribution();
    }
}
