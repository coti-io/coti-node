package io.coti.trustscore.bl.Decays;

import io.coti.trustscore.rulesData.TransactionEventScore;
import javafx.util.Pair;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.coti.trustscore.utils.DatesCalculation.calculateDaysDiffBetweenDates;

public class VectorDecaysTransactionCalculator {
    private DecayTransactionCalculator decayTransactionCalculator;
    private Map<TransactionEventScore, Map<Long, Pair<Double, Double>>> eventScoresToDatesScoreMap;

    public VectorDecaysTransactionCalculator(Map<TransactionEventScore, Map<Long, Pair<Double, Double>>> eventScoresToDatesScoreMap) {
        this.eventScoresToDatesScoreMap = eventScoresToDatesScoreMap;
        this.decayTransactionCalculator = new DecayTransactionCalculator();
    }

    public Map<TransactionEventScore, Double> calculateDatesVectorDecays(Date lastUpdate) {
        Map<TransactionEventScore, Double> eventScoresToTotalNewValueMap = new HashMap<>();
        for (Map.Entry<TransactionEventScore, Map<Long, Pair<Double, Double>>> eventScoresToDatesScoreMapEntry : eventScoresToDatesScoreMap.entrySet()) {
            TransactionEventScore transactionEventScore = eventScoresToDatesScoreMapEntry.getKey();
            double SumEventScore = 0.0;
            Map<Long, Pair<Double, Double>> dayToScoreMap = eventScoresToDatesScoreMapEntry.getValue();
            for (Map.Entry<Long, Pair<Double, Double>> dayToScoreMapEntry : dayToScoreMap.entrySet()) {
                int numberOfDecays = calculateDaysDiffBetweenDates(lastUpdate, new Date());
                double currentDailyScore = dayToScoreMapEntry.getValue().getValue();


                double decayedCurrentDailyScore = decayTransactionCalculator.calculateEntry(new TransactionEventDecay(transactionEventScore,currentDailyScore), numberOfDecays).getValue();
                dayToScoreMapEntry.setValue(new Pair<>(dayToScoreMapEntry.getValue().getKey(), decayedCurrentDailyScore));
                SumEventScore += decayedCurrentDailyScore;
            }
            eventScoresToTotalNewValueMap.put(transactionEventScore, SumEventScore);
        }
        return eventScoresToTotalNewValueMap;
    }
}
