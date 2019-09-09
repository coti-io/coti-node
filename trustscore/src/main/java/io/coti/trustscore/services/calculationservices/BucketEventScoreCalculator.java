package io.coti.trustscore.services.calculationservices;

import io.coti.trustscore.data.scorebuckets.BucketEventScoreData;
import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.data.parameters.EventCountAndContributionData;
import io.coti.trustscore.data.parameters.EventUserParameters;
import io.coti.trustscore.services.BucketEventScoreService;
import io.coti.trustscore.utils.DatesCalculation;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BucketEventScoreCalculator extends BucketScoresCalculator<BucketEventScoreData> {

    public BucketEventScoreCalculator(BucketEventScoreData bucketData) {
        super(bucketData);
    }

    public void setCurrentScores() {
        for (Map.Entry<FinalScoreType, EventCountAndContributionData> actualScoresDataMapEntry : bucketData.getActualScoresDataMap().entrySet()) {

            double tailContribution = 0;
            Map<LocalDate, Double> oldEventsDateMap = bucketData.getOldEventsScoreDateMap().get(actualScoresDataMapEntry.getKey());
            if (oldEventsDateMap != null) {
                tailContribution = oldEventsDateMap.values().stream().reduce(0.0, Double::sum);
            }

            int eventsCount = actualScoresDataMapEntry.getValue().getCountCurrent();
            double currentContribution = 0;

            switch (actualScoresDataMapEntry.getKey()) {
                case DOUBLESPENDING:
                case FALSEQUESTIONNAIRE:
                case INVALIDTX:
                    currentContribution = eventsCount;
                    break;
                case FILLQUESTIONNAIRE:
                    currentContribution = Math.min(Math.floor(eventsCount / 5), 3);
                    break;
            }

            actualScoresDataMapEntry.getValue().setContribution(currentContribution + tailContribution);
        }
    }

    protected void decayDailyScores(int daysDiff) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        for (Map.Entry<FinalScoreType, EventCountAndContributionData> actualScoresDataMapEntry : bucketData.getActualScoresDataMap().entrySet()) {
            EventUserParameters userParameters = BucketEventScoreService.userParameters(actualScoresDataMapEntry.getKey(), bucketData.getUserType());
            if (userParameters != null) {
                Map<LocalDate, Double> dayToScoreMap = new ConcurrentHashMap<>();

                if (actualScoresDataMapEntry.getValue().getCountCurrent() != 0) {
                    double decayedTodayScore = DatesCalculation.calculateDecay(userParameters.getSemiDecay(),
                            actualScoresDataMapEntry.getValue().getContribution(), daysDiff);
                    dayToScoreMap.put(bucketData.getLastUpdate(), decayedTodayScore);
                }

                Map<LocalDate, Double> oldScoresDateMap = bucketData.getOldEventsScoreDateMap().get(actualScoresDataMapEntry.getKey());

                for (Map.Entry<LocalDate, Double> dateToOldEventsContributionEntry : oldScoresDateMap.entrySet()) {
                    if (ChronoUnit.DAYS.between(dateToOldEventsContributionEntry.getKey(), today) <= userParameters.getTerm()) {
                        double decayedScore = DatesCalculation.calculateDecay(userParameters.getSemiDecay(),
                                dateToOldEventsContributionEntry.getValue(), daysDiff);
                        dayToScoreMap.put(dateToOldEventsContributionEntry.getKey(), decayedScore);
                    }
                }
                bucketData.getOldEventsScoreDateMap().put(actualScoresDataMapEntry.getKey(), dayToScoreMap);
                bucketData.getActualScoresDataMap().put(actualScoresDataMapEntry.getKey(),
                        new EventCountAndContributionData(0, 0));
            }
        }
    }

    public double getBucketSumScore() {
        double sumScore = 0;

        for (Map.Entry<FinalScoreType, EventCountAndContributionData> actualScoresDataMapEntry : bucketData.getActualScoresDataMap().entrySet()) {
            EventUserParameters userParameters = BucketEventScoreService.userParameters(actualScoresDataMapEntry.getKey(), bucketData.getUserType());
            if (userParameters != null) {
                sumScore += (actualScoresDataMapEntry.getValue().getContribution() * userParameters.getWeight());
            }
        }
        return sumScore;
    }
}
