package io.coti.trustscore.services.calculationservices;

import io.coti.trustscore.data.scorebuckets.BucketFrequencyBasedScoreData;
import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.data.parameters.FrequencyBasedCountAndContributionData;
import io.coti.trustscore.data.parameters.FrequencyBasedUserParameters;
import io.coti.trustscore.data.parameters.UserParameters;
import io.coti.trustscore.services.BucketFrequencyBasedScoreService;
import io.coti.trustscore.utils.DatesCalculation;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BucketFrequencyBasedScoreCalculator extends BucketScoresCalculator<BucketFrequencyBasedScoreData> {

    public BucketFrequencyBasedScoreCalculator(BucketFrequencyBasedScoreData bucketData) {
        super(bucketData);
    }

    public void setCurrentScores() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        for (Map.Entry<FinalScoreType, FrequencyBasedCountAndContributionData> actualScoresDataMapEntry : bucketData.getActualScoresDataMap().entrySet()) {
            FrequencyBasedUserParameters userParameters = BucketFrequencyBasedScoreService.userParameters(actualScoresDataMapEntry.getKey(), bucketData.getUserType());
            if (userParameters != null) {
                double tailContribution = 0;
                Map<LocalDate, Double> oldEventsDateMap = bucketData.getOldEventsScoreDateMap().get(actualScoresDataMapEntry.getKey());
                if (oldEventsDateMap != null) {
                    tailContribution = oldEventsDateMap.values().stream().reduce(0.0, Double::sum);
                }

                Map<LocalDate, Integer> oldCountsDateMap = bucketData.getOldEventsCountDateMap().get(actualScoresDataMapEntry.getKey());
                int currentCount = actualScoresDataMapEntry.getValue().getCountCurrent();
                double currentContribution = 0;

                switch (actualScoresDataMapEntry.getKey()) {
                    case CLAIM:
                        int oldCount = oldCountsDateMap.entrySet().stream()
                                .filter(entry -> ChronoUnit.DAYS.between(entry.getKey(), today) <= userParameters.getPeriod())
                                .mapToInt(Map.Entry::getValue).sum();
                        if (currentCount != 0 && (oldCount + currentCount) >= userParameters.getLimit()) {
                            currentContribution = Math.tanh((oldCount + currentCount) / userParameters.getLevel08()
                                    * UserParameters.atanh08);
                        }
                        break;
                }

                actualScoresDataMapEntry.getValue().setContributionCurrent(currentContribution);
                actualScoresDataMapEntry.getValue().setContributionTail(tailContribution);
            }
        }
    }

    protected void decayDailyScores(int daysDiff) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        for (Map.Entry<FinalScoreType, FrequencyBasedCountAndContributionData> actualScoresDataMapEntry : bucketData.getActualScoresDataMap().entrySet()) {
            FrequencyBasedUserParameters userParameters = BucketFrequencyBasedScoreService.userParameters(actualScoresDataMapEntry.getKey(), bucketData.getUserType());
            if (userParameters != null) {
                Map<LocalDate, Double> dayToScoreMap = new ConcurrentHashMap<>();
                Map<LocalDate, Double> oldScoresDateMap = bucketData.getOldEventsScoreDateMap().get(actualScoresDataMapEntry.getKey());
                Map<LocalDate, Integer> oldCountsDateMap = bucketData.getOldEventsCountDateMap().get(actualScoresDataMapEntry.getKey());

                if (actualScoresDataMapEntry.getValue().getCountCurrent() != 0) {
                    dayToScoreMap.put(bucketData.getLastUpdate(), DatesCalculation.calculateDecay(userParameters.getSemiDecay(),
                            actualScoresDataMapEntry.getValue().getContributionCurrent(), daysDiff));
                    oldCountsDateMap.put(bucketData.getLastUpdate(), actualScoresDataMapEntry.getValue().getCountCurrent());
                }

                for (Map.Entry<LocalDate, Double> dateToOldEventsContributionEntry : oldScoresDateMap.entrySet()) {
                    if (ChronoUnit.DAYS.between(dateToOldEventsContributionEntry.getKey(), today) <= userParameters.getTerm()) {
                        dayToScoreMap.put(dateToOldEventsContributionEntry.getKey(), DatesCalculation.calculateDecay(userParameters.getSemiDecay(),
                                dateToOldEventsContributionEntry.getValue(), daysDiff));
                    } else {
                        oldCountsDateMap.remove(dateToOldEventsContributionEntry.getKey());
                    }
                }
                bucketData.getOldEventsScoreDateMap().put(actualScoresDataMapEntry.getKey(), dayToScoreMap);
                bucketData.getActualScoresDataMap().put(actualScoresDataMapEntry.getKey(),
                        new FrequencyBasedCountAndContributionData(0, 0,0));
            }
        }
    }

    public double getBucketSumScore() {
        double sumScore = 0;

        for (Map.Entry<FinalScoreType, FrequencyBasedCountAndContributionData> actualScoresDataMapEntry : bucketData.getActualScoresDataMap().entrySet()) {
            FrequencyBasedUserParameters userParameters = BucketFrequencyBasedScoreService.userParameters(actualScoresDataMapEntry.getKey(), bucketData.getUserType());
            if (userParameters != null) {
                sumScore += (actualScoresDataMapEntry.getValue().getContributionCurrent()
                        + actualScoresDataMapEntry.getValue().getContributionTail()) * userParameters.getWeight();
            }
        }
        return sumScore;
    }
}
