package io.coti.trustscore.services.calculationservices;

import io.coti.trustscore.config.rules.EventScore;
import io.coti.trustscore.config.rules.TransactionEventScore;
import io.coti.trustscore.data.Enums.TransactionEventScoreType;
import javafx.util.Pair;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VectorScoreCalculator<T extends EventScore> {
    private Map<T, Map<Date, Double>> eventScoresMap;

    public VectorScoreCalculator(Map<T, Map<Date, Double>> eventScoresToDatesAndScoreMap) {
        this.eventScoresMap = eventScoresToDatesAndScoreMap;
    }

    public Map<T, Map<Date, Double>> calculateVectorScore() {
        Map<T, Map<Date, Double>> eventScoresToValueByDayMap = new ConcurrentHashMap<>();

        // Iterating on every event type
        for (Map.Entry<T, Map<Date, Double>> eventScoresToDatesScoreMapEntry : eventScoresMap.entrySet()) {
            T eventScore = eventScoresToDatesScoreMapEntry.getKey();
            Map<Date, Double> dayToScoreCalculateMap = eventScoresToDatesScoreMapEntry.getValue();
            Map<Date, Double> dailyScoreMap = new ConcurrentHashMap<>();

            // Iterating on every event day
            for (Map.Entry<Date, Double> dayToScoreCalculateEntry : dayToScoreCalculateMap.entrySet()) {
                Date day = dayToScoreCalculateEntry.getKey();
                double dailyScore = calculateFormulaTransactionEventScore((TransactionEventScore) eventScore, dayToScoreCalculateEntry.getValue());
                dailyScoreMap.put(day, dailyScore);
            }
            eventScoresToValueByDayMap.put(eventScore, dailyScoreMap);
        }
        return eventScoresToValueByDayMap;
    }

    private double calculateFormulaTransactionEventScore(TransactionEventScore transactionEventScore, double dayValue){
        return Math.tanh(dayValue/transactionEventScore.getLevel08()*transactionEventScore.getAtanh08());
    }

}
