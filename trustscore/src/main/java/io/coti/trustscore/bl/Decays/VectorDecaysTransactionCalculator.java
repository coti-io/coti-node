package io.coti.trustscore.bl.Decays;

import io.coti.trustscore.rulesData.TransactionEventScore;
import javafx.util.Pair;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.coti.trustscore.utils.DatesCalculation.calculateDaysdiffBetweenDates;

public class VectorDecaysTransactionCalculator {
    private DecayTransactionCalculator decayTransactionCalculator;
    private Map<TransactionEventScore, Map<Date, Double>> eventScoresToDatesScoreMap;

    public VectorDecaysTransactionCalculator(Map<TransactionEventScore, Map<Date, Double>> eventScoresToDatesScoreMap) {
        this.eventScoresToDatesScoreMap = eventScoresToDatesScoreMap;
        this.decayTransactionCalculator = new DecayTransactionCalculator();
    }

    public Map<TransactionEventScore, Double> calculateDatesVector() {
        Map<TransactionEventScore, Double> eventScoresToNewValueMap = new HashMap<>();
        for (Map.Entry<TransactionEventScore, Map<Date, Double>> eventScoresToDatesScoreMapEntry : eventScoresToDatesScoreMap.entrySet()) {
            TransactionEventScore transactionEventScore = eventScoresToDatesScoreMapEntry.getKey();
            double SumEventScore = 0.0;
            Map<Date, Double> dayToScoreMap = eventScoresToDatesScoreMapEntry.getValue();
            for (Map.Entry<Date, Double> dayToScoreMapEntry : dayToScoreMap.entrySet()) {
                int numberOfDecays = calculateDaysdiffBetweenDates(dayToScoreMapEntry.getKey(), new Date());
                double currentDailyScore = dayToScoreMapEntry.getValue();
                SumEventScore +=
                        decayTransactionCalculator.calculateEntry(new Pair<>(transactionEventScore, currentDailyScore), numberOfDecays)
                                .getValue();
            }
            eventScoresToNewValueMap.put(transactionEventScore, SumEventScore);
        }
        return eventScoresToNewValueMap;
    }
}
